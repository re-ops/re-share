(ns re-share.config.core
  "Configuration handling"
  (:refer-clojure :exclude  [load])
  (:require
   [clojure.core.strint :refer (<<)]))

(def config (atom nil))

(defn get!
  "Reading a keys path from configuration raises an error of keys not found"
  [& ks]
  {:pre [@config]}
  (if-let [v (get-in @config ks)]
    v
    (throw (ex-info (<< "No matching configuration keys ~{ks} found") {:keys ks :type ::missing-conf}))))

(defn get*
  "nil on missing version of get!"
  [& keys]
  {:pre [@config]}
  (get-in @config keys))

