(ns re-share.oshi
  (:require
   [taoensso.timbre :refer (refer-timbre set-level! merge-config!)]
   [cheshire.core :refer (parse-string)])
  (:import
   [oshi.json hardware.CentralProcessor SystemInfo util.PropertiesUtil]))

(refer-timbre)

(def si (SystemInfo.))

(def hal (.getHardware si))

(defn read-metrics
  ([]
   (read-metrics (PropertiesUtil/loadProperties "oshi.json.properties")))
  ([props]
   (parse-string (.toCompactJSON si props) true)))

(defn linux-type []
  (into {}
        (map (fn [line]
               (let [[f s] (clojure.string/split line #"=")]
                 [(keyword (clojure.string/lower-case f)) (clojure.string/replace s "\"" "")]))
             (line-seq (clojure.java.io/reader "/etc/os-release")))))

(defn os []
  (let [type (keyword (System/getProperty "os.name"))]
    (case type
      :Linux (keyword (:name (linux-type)))
      :default (throw (ex-info "OS type isnt supported" {})))))

(defn get-processes []
  (map bean (.getProcesses (.getOperatingSystem si) 0 nil)))

(comment
  (clojure.pprint/pprint (bean (.getComputerSystem hal)))
  (bean (.getProcessor hal)))
