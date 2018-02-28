(ns re-share.es.common
  "Common ES functions"
  (:refer-clojure :exclude (get))
  (:require
   [taoensso.timbre :refer (refer-timbre)]
   [qbits.spandex :as s]
   [re-share.es.node :refer (c)]))

(refer-timbre)

; Common ES functions

(defn exists-call
  [target]
  (try
    (= (:status (s/request @c {:url target :method :head})) 200)
    (catch Exception e
      (error e (ex-data e))
      false)))

(defn exists?
  ([index]
   (exists-call [index]))
  ([index t id]
   (exists-call [index t id])))

(defn delete-call
  [target]
  (try
    (= (:status (s/request @c {:url target :method :delete})) 200)
    (catch Exception e
      (error e (ex-data e))
      false)))

(defn delete
  ([index]
   (delete-call [index]))
  ([index t id]
   (delete-call [index t id])))

(defn delete-all
  [index t]
  (try
    (= (:status (s/request @c {:url [index t :_delete_by_query] :method :post :body {:query {:match_all {}}}})) 200)
    (catch Exception e
      (error e (ex-data e))
      false)))

(defn put-call
  [target m]
  (try
    (= (:status (s/request @c {:url target :method :put :body m})) 200)
    (catch Exception e
      (error e (ex-data e))
      false)))

(defn put [index t id m]
  (put-call [index t id] m))

(defn get [index t id]
  (try
    (get-in (s/request @c {:url [index t id] :method :get}) [:body :_source])
    (catch Exception e
      (when-not (= 404 (:status (ex-data e)))
        (throw e)))))

(def ^:const settings {:number_of_shards 1})

(defn create
  [index mappings]
  (= (:status (s/request @c {:url [index] :method :put :body mappings})) 200))

(defn clear
  "Creates systems index and type"
  [index]
  (when (exists? index)
    (info "Clearing index" index)
    (delete index)))

(defn all
  "An all query using match all on provided type, this should use scrolling for 10K systems"
  [index type]
  (let [query {:size 10000 :query {:match_all {}}}
        {:keys [body]} (s/request @c {:url [index type :_search] :method :get :body query})]
    (mapv (juxt :_id :_source) (get-in body [:hits :hits]))))

