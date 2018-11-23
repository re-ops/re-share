(ns re-share.es.common
  "Common ES functions"
  (:require
   [zentai.core :refer (conn-prefix)]
   [re-share.config :refer (get!)]
   [taoensso.timbre :refer (refer-timbre)]
   [clj-time.core :as t]
   [clj-time.format :as f]))

(refer-timbre)

(defn get-es! []
  (get! :shared :elasticsearch @conn-prefix))

(def day-format (f/formatter "yyyyMMdd"))

(defn with-day [day idx]
  (str idx "-" (f/unparse day-format day)))

(defn day-index
  "An index for the current day (for large indecies that are deleted after a fixed time range)"
  ([k t]
   (index k t (t/now)))
  ([k t d]
   (with-day d (index k t))))

(defn index
  "Index with key prefix and type postfix (since ES 6x onlys single type per index is supported)"
  [k t]
   (str (get! k :elasticsearch :index) "-" (name t)))
