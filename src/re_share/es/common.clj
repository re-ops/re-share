(ns re-share.es.common
  "Common ES functions"
  (:require
   [re-share.config :refer (get!)]
   [taoensso.timbre :refer (refer-timbre)]
   [clj-time.core :as t]
   [clj-time.format :as f]))

(refer-timbre)

(def conn-prefix (atom :default))

(defn get-es! []
  (get! :shared :elasticsearch @conn-prefix))

(def day-format (f/formatter "yyyyMMdd"))

(defn with-day [day idx]
  (str idx "-" (f/unparse day-format day)))

(defn index
  "index with key prefix and type postfix (since ES 6x onlys single type per index is supported)"
  ([k t]
   (index k t (t/now)))
  ([k t d]
   (with-day d (str (get! k :elasticsearch :index) "-" (name t)))))

