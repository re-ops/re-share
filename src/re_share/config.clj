(ns re-share.config
  "Configuration handling"
  (:refer-clojure :exclude  [load])
  (:require
   [clj-config.core :as conf]
   [clojure.pprint :refer (pprint)]
   [taoensso.timbre :refer (refer-timbre merge-config!)]
   [taoensso.timbre.appenders.core :refer (spit-appender)]
   [clojure.core.strint :refer (<<)]
   [clojure.java.io :refer (file)]))

(refer-timbre)

(def path
  (<< "~(System/getProperty \"user.home\")/.re-ops.edn"))

(defn pretty-error
  "Pretty print errors to log file"
  [k m c]
  (let [st (java.io.StringWriter.)]
    (binding [*out* st]
      (clojure.pprint/pprint m))
    (merge-config!
     {:appenders
      {:spit
       (spit-appender {:fname (get-in c [k :log :path] (<< "~(name k).log"))})}})
    (error "Following configuration errors found:\n" (.toString st))))

(defn read-and-validate [k f]
  (let [c (conf/read-config path) errors (f c)]
    (when-not (empty? errors)
      (pretty-error k errors c)
      (System/exit 1))
    c))

(def config (atom {}))

(defn load
  "Loading configuration for project k and validate using f"
  [k f]
  (info "Loading configuration")
  (if path
    (info (reset! config (read-and-validate k f)))
    (when-not (System/getProperty "disable-conf") ; enables repl/testing
      (error
       (<< "Missing configuration file, you should add ~{path}"))
      (System/exit 1))))

(defn get!
  "Reading a keys path from configuration raises an error of keys not found"
  [& ks]
  (if-let [v (get-in @config ks)]
    v
    (throw (ex-info (<< "No matching configuration keys ~{keys} found") {:keys ks :type ::missing-conf}))))

(defn get*
  "nil on missing version of get!"
  [& keys]
  (get-in @config keys))
