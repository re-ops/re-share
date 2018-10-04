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

(defn next-index [k mappings]
  "Create tommorow index"
  (watch :create-next-day-index (every-day 23)
         (fn []
           (let [tommorow (t/plus (t/now) (t/day 1))]
             (create-index (day-index k tommorow) mappings)))))

(defn purge-index [k t]
  "Clear index from last week"
  (watch :create-next-day-index (every-day 23)
         (fn []
           (let [last-week (t/minus (t/now) (t/day 7))]
             (delete (day-index k last-week) t)))))

(defn index-jobs [k mappings t]
  (next-index k mappings)
  (purge-index k t))
