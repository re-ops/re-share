(ns re-share.core
  "Common re-ops functions"
  (:import java.util.Date)
  (:require
   [minderbinder.time :refer  (parse-time-unit)]))

(defn find-port
  "find the first available port within a given range"
  [from to]
  (first
   (filter
    (fn [p] (try (with-open [s (java.net.ServerSocket. p)] true) (catch Exception e false))) (range from to))))

(defn curr-time [] (.getTime (Date.)))

(defn wait-for
  "A general wait for pred function
     (wait-for {:timeout [1 :minute] #() \"waiting for nothing failed\")}
  "
  [{:keys [timeout sleep] :or {sleep [1 :seconds]} :as timings} pred message]
  {:pre [(map? timings)]}
  (let [wait (+ (curr-time) (parse-time-unit timeout))]
    (loop []
      (if (> wait (curr-time))
        (if (pred)
          true
          (do (Thread/sleep (parse-time-unit sleep)) (recur)))
        (throw (ex-info message timings))))))

