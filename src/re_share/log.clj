(ns re-share.log
  "log collection"
  (:require
   [timbre-ns-pattern-level :as level]
   [clojure.string :refer (join upper-case)]
   [taoensso.timbre.appenders.3rd-party.rolling :refer (rolling-appender)]
   [taoensso.timbre.appenders.core :refer (println-appender)]
   [clansi.core :refer (style)]
   [taoensso.timbre :refer (refer-timbre set-level! merge-config!)]
   [clojure.core.strint :refer (<<)]
   [chime :refer [chime-ch]]
   [clj-time.core :as t]
   [clj-time.coerce :refer [to-long]]
   [clojure.java.io :refer (reader)]
   [re-share.schedule :refer (watch seconds)]))

(refer-timbre)

(defn run-purge [s]
  (watch :weekly-logs-purge (seconds s) (fn [] (trace "purging logs at" (t/now)) (debug "clearing weekly logs"))))

(def level-color
  {:info :green :debug :blue :error :red :warn :yellow})

(defn output-fn
  "Timbre logger format function"
  ([data] (output-fn nil data))
  ([opts data] ; For partials
   (let [{:keys [level ?err #_vargs msg_ ?ns-str ?file hostname_ timestamp_ ?line]} data]
     (str (style (upper-case (name level)) (level-color level)) " " (force timestamp_) " [" (style ?file :bg-black) "@" ?line "] "  ": " (force msg_)))))

(defn setup
  "See https://github.com/ptaoussanis/timbre"
  [n bs ws]
  ; disable-coloring
  (merge-config!
   {:output-fn (partial output-fn  {:stacktrace-fonts {}})})
  (merge-config! {:ns-blacklist ws})
  (merge-config! {:appenders {:println (merge {:ns-whitelist ws} (println-appender {:stream :auto}))
                              :rolling (rolling-appender {:path (str n ".log") :pattern :weekly})}})
  (merge-config!
   {:timestamp-opts {:timezone  (java.util.TimeZone/getDefault)}})
  (run-purge 10))

(defn debug-on
  ([] (set-level! :debug))
  ([n]
   (merge-config! {:middleware [(level/middleware {n :debug})]})))

(defn debug-off []
  (set-level! :info))

(defn redirect-output [n]
  (merge-config! {:appenders {:println (merge {:ns-whitelist n} (println-appender {:stream :auto}))}}))

(defn refer-share-logging []
  (require '[re-share.log :as share-log :refer (debug-on debug-off redirect-output)]))
