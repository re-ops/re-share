(ns re-share.es.common
  "Common ES functions"
  (:refer-clojure :exclude (get))
  (:require
   [clojure.string :refer (split)]
   [re-share.core :refer (error-m)]
   [re-share.config :refer (get!)]
   [taoensso.timbre :refer (refer-timbre)]
   [qbits.spandex :as s]
   [clj-time.core :as t]
   [clj-time.format :as f]
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
    (error-m e)
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
  "Delete all under index or a single id"
  ([index t]
   (delete-call [index t]))
  ([index t id]
   (delete-call [index t id])))

(defn delete-all
  [index]
  (try
    (ok (s/request (connection) {:url [index :_delete_by_query] :method :post :body {:query {:match_all {}}}}))
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
  "Persist instance m of and return generated id"
  [index t m]
  (try
    (let [{:keys [status body] :as resp} (s/request (connection) {:url [index t] :method :post :body m})]
      (when-not (ok resp)
        (throw (ex-info "failed to create" {:resp resp :m m :index index})))
      (body :_id))
    (catch Exception e
      (handle-ex e))))

(def ^:const settings {:number_of_shards 1})

(defn create-index
  "Create an index with provided mappings"
  [index mappings]
  (ok (s/request (connection) {:url [index] :method :put :body mappings})))

(defn list-indices []
  (let [ks [:health :status :index :uuid :pri :rep :docs.count :docs.deleted :store.size :pri.store.size]]
    (map #(zipmap ks (filter (comp not empty?) (split % #"\s")))
         (split (:body (s/request (connection) {:url [:_cat :indices] :method :get})) #"\n"))))

(defn clear
  "Clear index type"
  [index t]
  (when (exists? index)
    (info "Clearing index" index)
    (delete index t)))

(defn all
  "An all query using match all on provided index this should use scrolling for 10K systems"
  [index]
  (let [query {:size 10000 :query {:match_all {}}}
        {:keys [body]} (s/request (connection) {:url [index :_search] :method :get :body query})]
    (mapv (juxt :_id :_source) (get-in body [:hits :hits]))))

(defn delete-by
  "Delete by query like {:match {:type \"nmap scan\"}}"
  [index t query]
  (try
    (s/request (connection) {:url [index t :_delete_by_query] :method :post :body {:query query}})
    (catch Exception e
      (handle-ex e))))

(def conn-prefix (atom :default))

(defn get-es! []
  (get! :shared :elasticsearch @conn-prefix))

(defn prefix-switch
  "Change es prefix"
  [k]
  (reset! conn-prefix k))

(def day-format (f/formatter "yyyyMMdd"))

(defn with-day [day idx]
  (str idx "-" (f/unparse day-format day)))

(defn index
  "index with key prefix and type postfix (since ES 6x onlys single type per index is supported)"
  [k t]
  (with-day (t/now) (str (get! k :elasticsearch :index) "-" (name t))))

(defn day-index
  "index for a specific day"
  [k t day]
  (with-day day (index k t)))
