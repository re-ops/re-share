(ns re-share.schedule
  "Schedule tasks"
  (:require
   [clojure.core.strint :refer (<<)]
   [clansi.core :refer (style)]
   [clj-time.periodic :refer  [periodic-seq]]
   [taoensso.timbre :refer (refer-timbre)]
   [chime :refer [chime-at]]
   [clj-time.format :as f]
   [clj-time.coerce :as c]
   [clj-time.core :as t]
   [clj-time.local :refer [local-now to-local-date-time]]
   [clojure.core.async :as a :refer [<! go-loop close!]])
  (:import [org.joda.time DateTimeConstants DateTimeZone DateTime]))

(refer-timbre)

(def chs (atom {}))
(def status (atom {}))

(defn in [s]
  [(-> s t/seconds t/from-now)])

(defn seconds
  ([n f]
   (periodic-seq (t/plus (local-now) (t/seconds f)) (t/seconds n)))
  ([n]
   (periodic-seq (local-now) (t/seconds n))))

(defn every-day [hour]
  (let [^DateTime now (local-now) dates (periodic-seq (.. now (withTime hour 0 0 0)) (t/days 1))]
    (if (> (c/to-long (first dates)) (c/to-long now)) dates (rest dates))))

(defn on-weekdays [hour]
  (remove (comp #{DateTimeConstants/SATURDAY DateTimeConstants/SUNDAY} #(.getDayOfWeek ^DateTime %))
          (every-day hour)))

(defn at-day [day hour]
  (filter (comp #{day} #(.getDayOfWeek ^DateTime %)) (every-day hour)))

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
                   {:on-finished (fn [] (debug "job done" k))})))

(defn halt!
  ([]
   (doseq [[k f] @chs] (halt! k)))
  ([k]
   (debug "closing channel")
   (close! (@chs k))
   (debug "clearing chs and status atoms")
   (swap! chs dissoc k)
   (swap! status dissoc k)))

(defn local-str [t]
  (f/unparse (f/formatter-local "dd/MM/YY HH:mm:ss") t))

(defn next-run []
  (doseq [[k {:keys [result period]}] (sort-by (fn [[k m]] (first (m :period))) @status)]
    (let [date (local-str (first period))]
      (println (style date :blue) (<< " ~(name k)")))))
