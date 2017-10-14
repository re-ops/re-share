(ns re-share.zero.events
  (:require
   [taoensso.timbre :refer (refer-timbre)])
  (:import
   [org.zeromq ZMQ ZMQ$Event ZMQ$Poller]))

(refer-timbre)

(def t (atom nil))

(def flag (atom true))

(def types
  {ZMQ/EVENT_CONNECTED :connected
   ZMQ/EVENT_CONNECT_DELAYED :delayed
   ZMQ/EVENT_DISCONNECTED :disconnected
   ZMQ/EVENT_CLOSED :closed
   ZMQ/EVENT_CONNECT_RETRIED :retried})

(defn handle-event [socket handler]
  (let [event (ZMQ$Event/recv socket ZMQ/EVENT_CONNECTED)]
    (if event
      (let [e-type (get types (.getEvent event) :unknown)]
        (handler e-type event)
        (debug "Event" e-type))
      (Thread/sleep 1000))))

(defn events-loop [ctx handler]
  (let [socket (.socket ctx ZMQ/PAIR)]
    (try
      (.connect socket "inproc://events")
      (info "events loop running")
      (while @flag
        (handle-event socket handler))
      (catch Exception e
        (error (.getMessage e) (.getStacktrace e)))
      (finally
        (.setLinger socket 0)
        (.close socket)
        (info "events loop is down")))))

(defn start [ctx handler]
  (reset! flag true)
  (reset! t (future (events-loop ctx handler))))

(defn stop []
  (reset! flag false)
  (when @t
    (reset! t nil)))
