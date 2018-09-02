(ns re-share.es.common
  "Common ES functions"
  (:refer-clojure :exclude (get))
  (:require
   [re-share.config :refer (get!)]
   [taoensso.timbre :refer (refer-timbre)]
   [qbits.spandex :as s]
   [re-share.es.node :refer (connection)]))

(refer-timbre)

; Common ES functions

(defn- ok [resp]
  (#{200 201} (:status resp)))

(defn- illegal [e]
  (instance? java.lang.IllegalStateException e))

; when we reset the connection
(defn- reactor-stopped [e]
  (let [c "Request cannot be executed; I/O reactor status: STOPPED"]
    (and (illegal e) (= (-> e Throwable->map :cause) c))))

(defn- handle-ex [e]
  (when-not (reactor-stopped e)
    (error e (ex-data e))
    (throw e)))

(defn exists-call
  [target]
  (try
    (ok (s/request (connection) {:url target :method :head}))
    (catch Exception e
      (when-not (= 404 (:status (ex-data e)))
        (handle-ex e)))))

(defn exists?
  "Check if index exists or instance with id existing within an index"
  ([index]
   (exists-call [index]))
  ([index t id]
   (exists-call [index t id])))

(defn delete-call
  [target]
  (try
    (ok (s/request (connection) {:url target :method :delete}))
    (catch Exception e
      (handle-ex e))))

(defn delete
  ([index]
   (delete-call [index]))
  ([index t id]
   (delete-call [index t id])))

(defn delete-all
  [index t]
  (try
    (ok (s/request (connection) {:url [index t :_delete_by_query] :method :post :body {:query {:match_all {}}}}))
    (catch Exception e
      (handle-ex e))))

(defn put-call
  [target m]
  (try
    (ok (s/request (connection) {:url target :method :put :body m}))
    (catch Exception e
      (handle-ex e))))

(defn put [index t id m]
  (put-call [index t id] m))

(defn get [index t id]
  (try
    (get-in (s/request (connection) {:url [index t id] :method :get}) [:body :_source])
    (catch Exception e
      (when-not (= 404 (:status (ex-data e)))
        (handle-ex e)))))

(defn create
  "Persist instance m of type t and return generated id"
  [index t m]
  (try
    (let [{:keys [status body] :as resp} (s/request (connection) {:url [index t] :method :post :body m})]
      (when-not (ok resp)
        (throw (ex-info "failed to create" {:resp resp :m m :t t :index index})))
      (body :_id))
    (catch Exception e
      (handle-ex e))))

(def ^:const settings {:number_of_shards 1})

(defn create-index
  "Create an index with provided mappings"
  [index mappings]
  (ok (s/request (connection) {:url [index] :method :put :body mappings})))

(defn clear
  "Clear index and type"
  [index]
  (when (exists? index)
    (info "Clearing index" index)
    (delete index)))

(defn all
  "An all query using match all on provided type, this should use scrolling for 10K systems"
  [index type]
  (let [query {:size 10000 :query {:match_all {}}}
        {:keys [body]} (s/request (connection) {:url [index type :_search] :method :get :body query})]
    (mapv (juxt :_id :_source) (get-in body [:hits :hits]))))

(defn delete-by
  "Delete by query like {:match {:type \"nmap scan\"}}"
  [index type query]
  (try
    (s/request (connection) {:url [index type :_delete_by_query] :method :post :body {:query query}})
    (catch Exception e
      (handle-ex e))))

(def conn-prefix (atom :default))

(defn get-es! []
  (get! :shared :elasticsearch @conn-prefix))

(defn prefix-switch
  "Change es prefix"
  [k]
  (reset! conn-prefix k))

(defn index [k]
  (get! k :elasticsearch :index))
