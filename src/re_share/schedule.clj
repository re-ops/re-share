(ns re-share.schedule
  "Schedule tasks"
  (:require
   [re-share.time :refer (into-zoned midnight local-now local-str)]
   [clojure.core.strint :refer (<<)]
   [clansi.core :refer (style)]
   [taoensso.timbre :refer (refer-timbre)]
   [chime.core :refer [chime-at periodic-seq]])
  (:import
   [java.time Duration]))

(refer-timbre)

(def chs (atom {}))
(def status (atom {}))

(defn seconds
  [n]
  (periodic-seq (local-now) (Duration/ofSeconds n)))

(defn every-day
  "Every day at the specified hour"
  [hour]
  (filter
   (fn [date]
     (= (.getHour (into-zoned date)) hour)) (periodic-seq (local-now) (Duration/ofHours 1))))

(defn at-day
  "At a specific day of the week and hour"
  [day hour]
  (filter (fn [date] (= (.getDayOfWeek (into-zoned date)) day)) (every-day hour)))

(defn every-nth-day
  "At an nth day and hour
    ; Every Second Friday at 23:00
    (every-nth-day 2 DayOfWeek/Friday 22)
  "
  [nt day hour]
  (keep-indexed (fn [i date] (when (= (mod i nt) 0) date)) (at-day day hour)))

(defn halt!
  ([]
   (doseq [[k f] @chs] (halt! k)))
  ([k]
   (when-let [c (@chs k)]
     (.close c)
     (debug (<< "closed ~{k}"))
     (swap! chs dissoc k)
     (debug (<< "cleared ~{k}")))))

(defn watch
  "run f using provided period"
  [k period f & args]
  (try
    (when (@chs k)
      (halt! k))
    (swap! status assoc k {:period period})
    (swap! chs assoc k
           (chime-at period
                     (fn [t]
                       (trace "chiming at" t)
                       (try
                         (let [result (apply f args)]
                           (swap! status update k
                                  (fn [{:keys [period] :as m}] (merge m {:result result :time (local-now) :period (rest period)}))))
                         (catch Throwable t
                           (error (<< "failed to run ~{k}") t))))
                     {:on-finished
                      (fn []
                        (swap! status dissoc k)
                        (info "halt of" k "done"))
                      :error-handler
                      (fn [e]
                        (error (<< "scheduled run of ~{k} has failed") e) true)}))
    (catch Throwable t
      (error (<< "failed to schedule ~{k}") t)
      (throw t))))

(defn next-run []
  (doseq [[k {:keys [period]}] (sort-by (fn [[k m]] (first (m :period))) @status)]
    (let [date (local-str (first period))]
      (when date
        (println (style date :blue) (<< " ~(name k)"))))))
