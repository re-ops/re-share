(ns re-share.time
  "Common time functions"
  (:import
   [java.time ZonedDateTime ZoneId Period LocalTime DayOfWeek Duration]
   [java.time.temporal ChronoUnit]
   [java.time.format DateTimeFormatter]))

(defn into-zoned  [instant]
  (.atZone instant (ZoneId/systemDefault)))

(defn midnight
  "Get todays midnight"
  []
  (.toInstant (.adjustInto (LocalTime/of 0 0) (ZonedDateTime/now (ZoneId/systemDefault)))))

(defn local-now []
  (.toInstant (ZonedDateTime/now)))

(defn local-str [t]
  (when t
    (.format (DateTimeFormatter/ofPattern "dd/MM/YY HH:mm:ss") (into-zoned t))))

(defn format-time [f t]
  (when t
    (.format (DateTimeFormatter/ofPattern f) (into-zoned t))))

(defn minus [instant amount unit]
  (.minus instant amount unit))

(defn to-long [instant]
  (.toEpochMilli instant))

(comment
  (minus (local-now) 1 ChronoUnit/MINUTES)
  (local-str (local-now))
  (format-time "dd-MM-YYYY_HH:mm:ss_SS" (local-now)))
