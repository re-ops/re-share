(ns re-share.core
  "Common re-ops functions"
  (:require
   [taoensso.timbre :refer  (refer-timbre)]
   [minderbinder.time :refer  (parse-time-unit)])
  (:import
   java.io.StringWriter
   java.io.PrintWriter
   java.util.Date
   java.security.MessageDigest
   java.math.BigInteger))

(refer-timbre)

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

(defn md5 [^String s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn error-m [e]
  (let [sw (StringWriter.) p (PrintWriter. sw)]
    (.printStackTrace e p)
    (error (.getMessage e) (.toString sw))))
