(ns re-share.es.common
  "Common ES functions"
  (:require
   [clojure.core.strint :refer (<<)]
   ; cleanup scheduling
   [re-share.schedule :refer (watch every-day)]
   [rubber.core :refer (conn-prefix exists? create-index delete-index)]
   [re-share.config.core :refer (get!)]
   [taoensso.timbre :refer (refer-timbre)]
   [clj-time.core :as t]
   [clj-time.format :as f]))

(refer-timbre)

(defn get-es! []
  (get! :shared :elasticsearch @conn-prefix))

(def day-format (f/formatter "yyyyMMdd"))

(defn with-day [day idx]
  (str idx "-" (f/unparse day-format day)))

(defn index
  "Index with key prefix and type postfix (since ES 6x onlys single type per index is supported)"
  [k t]
  (str (get! k :elasticsearch :index) "-" (name t)))

(defn day-index
  "An index for the current day (for large indecies that are deleted after a fixed time range)"
  ([k t]
   (day-index k t (t/now)))
  ([k t d]
   (with-day d (index k t))))

(defn next-index
  "Create next index for each type"
  [k mappings]
  (watch :create-next-day-index (every-day 23)
         (fn []
           (let [tommorow (t/plus (t/now) (t/days 1))]
             (doseq [[t m] mappings]
               (info "creating new index" (day-index k t tommorow) {:mappings {t m}})
               (create-index (day-index k t tommorow) {:mappings {t m}}))))))

(defn purge-index
  "Clear old index from last week"
  [k mappings]
  (watch :clear-last-week-index (every-day 23)
         (fn []
           (let [last-week (t/minus (t/now) (t/days 7))]
             (doseq [[t _] mappings
                     :let [idx (keyword (day-index k t last-week))]]
               (when (exists? idx)
                 (info "clearing old index" idx)
                 (delete-index idx)))))))

(defn setup-index-jobs [k mappings]
  (next-index k mappings)
  (purge-index k mappings))

(defn initialize
  "Creates systems index and types"
  [parent types daily?]
  (doseq [[k t] types]
    (let [f (if daily? day-index index)
          idx (f parent k)]
      (when-not (exists? idx)
        (info "Creating index" idx)
        (create-index idx {:mappings {k t}}))
      (when daily?
        (setup-index-jobs parent {k t})))))
