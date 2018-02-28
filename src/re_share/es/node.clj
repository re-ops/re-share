(ns re-share.es.node
  "Elasticsearch node management"
  (:require
   [clojure.core.strint :refer (<<)]
   [qbits.spandex :as s]
   [taoensso.timbre :refer (refer-timbre)]
   [safely.core :refer [safely]]))

(refer-timbre)

(def c (atom nil))

(def snif (atom nil))

(defn connect-
  "Connecting to Elasticsearch"
  [{:keys [host port]}]
  (when-not @c
    (info "Connecting to elasticsearch using http://~{host}:~{port}")
    (reset! c
            (s/client {:hosts [(<< "http://~{host}:~{port}")]
                       :basic-auth {:user "elastic" :password "changeme"}}))
    (reset! snif (s/sniffer @c))))

(defn connect
  "Connecting to Elasticsearch with retry support"
  [{:keys [host port]}]
  (safely (connect-)
          :on-error
          :max-retry 5
          :message "Error while trying to connect to Elasticsearch"
          :log-errors true
          :retry-delay [:random-range :min 2000 :max 5000]))

(defn stop
  "Reset connection atom"
  []
  (info "Closing elasticsearch connection")
  (when @c
    (s/close! @c)
    (reset! c nil))
  (when @snif
    (s/close! @snif)
    (reset! snif nil)))

(defn connection []
  (if @c
    @c
    (throw (ex-info "no connection is set for Elasticsearch"))))
