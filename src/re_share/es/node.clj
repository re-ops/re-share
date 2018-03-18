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

(defn connection []
  (if @c
    @c
    (throw (ex-info "no connection is set for Elasticsearch" {}))))

(defn health
  "get cluster health"
  []
  (:body (s/request (connection) {:url ["_cluster" "health"] :method :get})))

(defn check
  "check the connection is working and cluster is healthy"
  []
  (try
    (let [h (health)]
      (if (= "red" (h :status))
        (throw (ex-info "Elasticsearch is read" h))))
    (catch java.net.ConnectException e
      (throw (ex-info "Elasticsearch is down" {})))))

(defn connect
  "Connecting to Elasticsearch"
  [{:keys [host port user pass] :as m}]
  (when-not @c
    (info (<< "Connecting to elasticsearch using http://~{host}:~{port}"))
    (reset! c
            (s/client {:hosts [(<< "http://~{host}:~{port}")]
                       :basic-auth {:user user :password pass}}))
    (check)
    (reset! snif (s/sniffer @c))))

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
