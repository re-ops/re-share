(ns re-share.es.cleanup
  "Periodcal ES index cleanup and creation"
  (:require
   [re-share.es.common :refer (create-index day-index delete)]
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
               (create-index (day-index k t tommorow) {t m}))))))

(defn purge-index
  "Clear index from last week"
  [k mappings]
  (watch :clear-last-week-index (every-day 23)
         (fn []
           (let [last-week (t/minus (t/now) (t/day 7))]
             (doseq [[t m] mappings]
               (delete (day-index k t last-week) {t m}))))))

(defn setup-index-jobs [k mappings]
  (next-index k mappings)
  (purge-index k (first (keys mappings))))
