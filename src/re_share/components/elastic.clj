(ns re-share.components.elastic
  "Elasticsearch component"
  (:require
   [taoensso.timbre :refer (refer-timbre)]
   [re-share.components.core :refer (Lifecyle)]
   [rubber.node :as node]
   [rubber.core :refer (exists? create-index)]
   [re-share.es.cleanup :as clean]
   [re-share.es.common :as common :refer (get-es!)]))

(refer-timbre)

(defn initialize
  "Creates systems index and types"
  [parent types daily?]
  (doseq [[k t] types]
    (let [f (if daily? common/day-index common/index)
          idx (f parent k)]
      (when-not (exists? idx)
        (info "Creating index" idx)
        (create-index idx {:mappings {k t}}))
      (when daily?
        (clean/setup-index-jobs parent {k t})))))

(defrecord Elastic [types parent daily?]
  Lifecyle
  (setup [this]
    (node/connect (get-es!))
    (initialize parent types daily?))
  (start [this]
    (node/connect (get-es!)))
  (stop [this]
    (node/stop)))

(defn instance
  "creates a Elastic components"
  [types parent daily?]
  (Elastic. types parent daily?))

