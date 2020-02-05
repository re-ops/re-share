(ns re-share.oshi
  (:require
   [clojure.java.data :as j]
   [taoensso.timbre :refer (refer-timbre set-level! merge-config!)])
  (:import
   [oshi hardware.CentralProcessor SystemInfo]))

(refer-timbre)

(def si (SystemInfo.))

(defn hardware
  "Get all hardware information from oshi"
  []
  (j/from-java (.getHardware si)))

(defn operating-system
  "Get all operating system information from oshi"
  []
  (j/from-java (.getOperatingSystem si)))

(defn read-all
  "Get all system information using oshi"
  []
  (j/from-java si))

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

(defn get-processes
  "Get running processes information using oshi"
  []
  (j/from-java (.getProcesses (.getOperatingSystem si) 0 nil)))

