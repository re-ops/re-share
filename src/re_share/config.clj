(ns re-share.config
  "Configuration handling"
  (:refer-clojure :exclude  [load])
  (:require
   [expound.alpha :as expound]
   [clojure.spec.alpha :as s]
   [aero.core :as aero]
   [taoensso.timbre :refer (refer-timbre)]
   [clojure.core.strint :refer (<<)]
   [clojure.java.io :refer (file)]))

(refer-timbre)

(s/def ::index string?)

(s/def ::elasticsearch (s/keys :req-un [::index]))

(s/def ::re-mote (s/keys :req-un [::elasticsearch]))

(s/def ::re-core (s/keys :req-un [::elasticsearch]))

(def path
  (<< "~(System/getProperty \"user.home\")/.re-ops.edn"))

(defn pretty-error
  "Pretty print errors to log file"
  [m c]
  (let [st (java.io.StringWriter.)]
    (binding [*out* st]
      (clojure.pprint/pprint m))
    (println "Following configuration errors found:\n" (.toString st))))

(def config
  (aero/read-config path))

(defn get!
  "Reading a keys path from configuration raises an error of keys not found"
  [& ks]
  (if-let [v (get-in config ks)]
    v
    (throw (ex-info (<< "No matching configuration keys ~{ks} found") {:keys ks :type ::missing-conf}))))

(defn get*
  "nil on missing version of get!"
  [& keys]
  (get-in config keys))
