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

(defn os []
  (keyword (get-in (read-metrics) [:operatingSystem :family])))

(defn get-processes []
   (map bean (.getProcesses (.getOperatingSystem si) 0 nil)))

(comment
  (clojure.pprint/pprint (bean (.getComputerSystem hal)))
  (bean (.getProcessor hal)))
