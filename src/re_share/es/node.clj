(ns re-share.es.node
  "Elasticsearch node management"
  (:require
   [clojure.core.strint :refer (<<)]
   [qbits.spandex :as s]
   [taoensso.timbre :refer (refer-timbre)]
   [safely.core :refer [safely]])
  (:import
   [org.apache.http.ssl SSLContextBuilder]
   [org.apache.http.conn.ssl TrustSelfSignedStrategy]))

(refer-timbre)

(def c (atom nil))

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

(defn self-signed-context
  "trusting self signed ssl certs"
  []
  (.build (.loadTrustMaterial (SSLContextBuilder/create) (TrustSelfSignedStrategy.))))

(defn add-context [m]
  (assoc-in m [:http-client :ssl-context] (self-signed-context)))

(defn connect
  "Connecting to Elasticsearch"
  [{:keys [host auth self?]}]
  (when-not @c
    (info (<< "Connecting to elasticsearch ~{host}"))
    (let [m {:hosts [host] :http-client {:auth-caching? true :basic-auth auth}}]
      (reset! c
              (s/client (if self? (add-context m) m)))
      (check))))

(defn stop
  "Reset connection atom"
  []
  (info "Closing elasticsearch connection")
  (when @c
    (s/close! @c)
    (reset! c nil)))
