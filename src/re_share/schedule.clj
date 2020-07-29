(ns re-share.schedule
  "Schedule tasks"
  (:require
   [clojure.core.strint :refer (<<)]
   [clansi.core :refer (style)]
   [taoensso.timbre :refer (refer-timbre)]
   [chime.core :refer [chime-at periodic-seq]])
  (:import
   [java.time ZonedDateTime ZoneId Period LocalTime DayOfWeek Duration]
   [java.time.format DateTimeFormatter]))

(refer-timbre)

(def chs (atom {}))
(def status (atom {}))

(defn into-zoned  [instant]
  (.atZone instant (ZoneId/systemDefault)))

(defn midnight
  "Get todays midnight"
  []
  (.toInstant (.adjustInto (LocalTime/of 0 0) (ZonedDateTime/now (ZoneId/systemDefault)))))

(defn local-now []
  (.toInstant (ZonedDateTime/now)))

(defn seconds
  [n]
  (periodic-seq (local-now) (Duration/ofSeconds n)))

(defn every-day
  "Every day at the specified hour"
  [hour]
  (filter
   (fn [date]
     (= (.getHour (into-zoned date)) hour)) (periodic-seq (midnight) (Duration/ofHours 1))))

(defn at-day
  "At a specific day of the week and hour"
  [day hour]
  (filter (fn [date] (= (.getDayOfWeek (into-zoned date)) day)) (every-day hour)))

(defn watch
  "run f using provided period"
  [k period f & args]
  (swap! status assoc k {:period period})
  (swap! chs assoc k
         (chime-at period
                   (fn [t]
                     (trace "chime" t)
                     (let [result (apply f args)]
                       (swap! status update k
                              (fn [{:keys [period] :as m}] (merge m {:result result :time (local-now) :period (rest period)})))))
                   {:on-finished
                    (fn []
                      (swap! status dissoc k)
                      (info "job done" k))
                    :error-handler
                    (fn [e]
                      (error e)
                      (throw e))})))
(defn halt!
  ([]
   (doseq [[k f] @chs] (halt! k)))
  ([k]
   (debug (<< "closing ~{k}"))
   (.close (@chs k))
   (debug (<< "clearing ~{k}"))
   (swap! chs dissoc k)))

(defn local-str [t]
  (when t
    (.format (DateTimeFormatter/ofPattern "dd/MM/YY HH:mm:ss") (into-zoned t))))

(defn next-run []
  (doseq [[k {:keys [period]}] (sort-by (fn [[k m]] (first (m :period))) @status)]
    (let [date (local-str (first period))]
      (when date
        (println (style date :blue) (<< " ~(name k)"))))))
