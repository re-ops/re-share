(ns re-share.components.elastic
  "Elasticsearch component"
  (:require
   [taoensso.timbre :refer (refer-timbre)]
   [re-share.components.core :refer (Lifecyle)]
   [re-share.es.node :as node]
   [re-share.es.common :as common :refer (get-es! exists? create-index)]))

(refer-timbre)

(defn initialize
  "Creates systems index and types"
  [index types]
  (when-not (exists? index)
    (info "Creating index" index)
    (create-index index {:mappings types})))

(defrecord Elastic [types k]
  Lifecyle
  (setup [this]
    (node/connect (get-es!))
    (initialize (common/index k) types))
  (start [this]
    (node/connect (get-es!)))
  (stop [this]
    (node/stop)))

(defn instance
  "creates a Elastic components"
  [types k]
  (Elastic. types k))

