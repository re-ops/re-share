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
  [parent types]
  (doseq [[k t] types]
    (let [idx (common/index parent k)]
      (when-not (exists? idx)
        (info "Creating index" idx)
        (create-index idx {:mappings {k t}})))))

(defrecord Elastic [types parent]
  Lifecyle
  (setup [this]
    (node/connect (get-es!))
    (initialize parent types))
  (start [this]
    (node/connect (get-es!)))
  (stop [this]
    (node/stop)))

(defn instance
  "creates a Elastic components"
  [types parent]
  (Elastic. types parent))

