(ns re-share.zero.common
  (:require
   [clojure.core.strint :refer  (<<)]
   [re-share.zero.keys :refer (read-key paths)]
   [re-share.core :refer (error-m)]
   )
  (:import
   [org.zeromq ZMQ]))

(defn context [] (ZMQ/context 1))

(defn close [s]
  (try
    (.setLinger s 0)
    (.close s)
    (catch Exception e
      (error-m e))))

(defn close! [sockets]
  (doseq [[k s] sockets] (close s)))

(defn client-socket [ctx t parent]
  (let [{:keys [server-public client-public client-private]} (paths parent)]
    (doto (.socket ctx t)
      (.setZapDomain (.getBytes "global"))
      (.setCurveServerKey (read-key server-public))
      (.setCurvePublicKey (read-key client-public))
      (.setCurveSecretKey (read-key client-private)))))

(comment
  (read-key ".curve/client-private.key")
  (alength (read-key ".curve/client-public.key")))
