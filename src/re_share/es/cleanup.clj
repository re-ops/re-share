(ns re-share.es.cleanup
  "Periodcal ES index cleanup and creation"
  (:require
   [zentai.core :refer (create-index delete-index exists?)]
   [re-share.es.common :refer (index)]
   [clojure.core.strint :refer (<<)]
   [taoensso.timbre :refer (refer-timbre)]
   [clojure.core.strint :refer (<<)]
   [chime :refer [chime-ch]]
   [clj-time.core :as t]
   [re-share.schedule :refer (watch every-day)]))

(refer-timbre)

(defn next-index
  "Create tommorow index for each type"
  [k mappings]
  (watch :create-next-day-index (every-day 23)
         (fn []
           (let [tommorow (t/plus (t/now) (t/days 1))]
             (doseq [[t m] mappings]
               (create-index (index k t tommorow) {:mappings {t m}}))))))

(defn purge-index
  "Clear index from last week"
  [k mappings]
  (watch :clear-last-week-index (every-day 23)
         (fn []
           (let [last-week (t/minus (t/now) (t/days 7))]
             (doseq [[t m] mappings
                     :let [idx (keyword (index k t last-week))]]
               (when-not (exists? idx)
                 (delete-index idx)))))))

(defn setup-index-jobs [k mappings]
  (next-index k mappings)
  (purge-index k mappings))
