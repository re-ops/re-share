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
  [types]
  (doseq [[k t :as m] types]
    (let [index (common/index k t)]
      (when-not (exists? index)
        (info "Creating index" index)
        (create-index index {:mappings m})))))

(defrecord Elastic [types k]
  Lifecyle
  (setup [this]
    (node/connect (get-es!))
    (initialize types))
  (start [this]
    (node/connect (get-es!)))
  (stop [this]
    (node/stop)))

(defn instance
  "creates a Elastic components"
  [types parent]
  (Elastic. types parent))

