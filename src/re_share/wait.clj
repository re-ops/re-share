(ns re-share.wait
  (:require
   [taoensso.timbre :refer  (refer-timbre)]
   [minderbinder.time :refer  (parse-time-unit)])
  (:import
   java.util.Date))

(refer-timbre)

(defn wait-time [stamp timeout]
  (+ stamp (parse-time-unit timeout)))

(defn curr-time [] (.getTime (Date.)))

(def flag (atom true))

(defn wait-for
  "A general wait for pred function
     (wait-for {:timeout [1 :minute] #() \"waiting for nothing failed\")}
  "
  [{:keys [timeout sleep] :or {sleep [100 :ms]} :as timings} pred message]
  {:pre [(map? timings)]}
  (let [wait (wait-time (curr-time) timeout)]
    (loop []
      (if (> wait (curr-time))
        (if (pred)
          true
          (when @flag
            (do (Thread/sleep (parse-time-unit sleep)) (recur))))
        (throw (ex-info message timings))))))

(defn stop-waits []
  (reset! flag false))

(defn enable-waits []
  (reset! flag true))
