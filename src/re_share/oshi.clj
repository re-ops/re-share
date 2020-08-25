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
  (let [hardware (.getHardware si)
        usb (.getUsbDevices hardware true)]
    (assoc (j/from-java hardware) :usbDevices (j/from-java usb))))

(defn operating-system
  "Get all operating system information from oshi"
  []
  (j/from-java (.getOperatingSystem si)))

(defn read-all
  "Get all system information using oshi"
  []
  (j/from-java si))

(defn get-processes
  "Get running processes information using oshi"
  []
  (j/from-java (.getProcesses (.getOperatingSystem si) 0 nil)))

