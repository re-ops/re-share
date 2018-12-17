(ns re-share.log
  "log collection"
  (:require
   [clojure.core.strint :refer (<<)]
   [me.raynes.fs :refer (glob delete)]
   [timbre-ns-pattern-level :as level]
   [clojure.string :refer (join upper-case)]
   [taoensso.timbre.appenders.3rd-party.rolling :refer (rolling-appender)]
   [taoensso.timbre.appenders.3rd-party.logstash :refer (logstash-appender)]
   [taoensso.timbre.appenders.core :refer (println-appender)]
   [clansi.core :refer (style)]
   [taoensso.timbre :refer (refer-timbre set-level! merge-config! set-config!)]
   [chime :refer [chime-ch]]
   [clj-time.core :as t]
   [clj-time.format :as f]
   [clojure.java.io :refer (reader)]
   [re-share.schedule :refer (watch seconds)]))

(refer-timbre)

(defn- this-week [{:keys [date]}]
  (t/within? (t/interval (t/minus (t/now) (t/weeks 1)) (t/now)) date))

(defn- log-date [[f s]]
  {:file f :date (f/parse (f/formatter "yyyyMMdd") s)})

(defn old-logs
  "Logs older than one week"
  []
  (filter (comp not this-week)
          (map log-date (filter identity (map #(re-matches #".*log.(\d{8}).*" (.getName %))  (glob "*log.*"))))))

(defn purge-logs
  "Purge logs older than one week"
  []
  (let [cleared (old-logs)]
    (doseq [{:keys [file]} cleared]
      (trace (<< "purging ~{file}"))
      (delete file))
    (debug (<< "purged ~(count cleared) log files"))))

(defn run-purge [s]
  (watch :weekly-logs-purge (seconds s) purge-logs))

(def level-color
  {:info :green :debug :blue :error :red :warn :yellow})

(defn colourful-log
  "A colorful human friendly output scheme"
  ([data] (colourful-log nil data))
  ([opts data] ; For partials
   (let [{:keys [level ?err #_vargs msg_ ?ns-str ?file hostname_ timestamp_ ?line]} data]
     (str (style (upper-case (name level)) (level-color level)) " " (force timestamp_) " [" (style ?file :bg-black) "@" ?line "] "  ": " (force msg_)))))

(defn basic-log
  "A simple logging shceme useful in central logging"
  ([data] (basic-log nil data))
  ([opts data] ; For partials
   (let [{:keys [level ?err #_vargs msg_ ?ns-str ?file hostname_ timestamp_ ?line]} data]
     (str (upper-case (name level)) " " (force timestamp_) " [" ?file "@" ?line "] "  ": " (force msg_)))))

(defn setup
  "Setting up logging:
    * file - file log output
    * blacklisted - namespaces whos log won't be included
    * stdout - namespaces whoe logs will be included in stdout
   Optional:
    * output-fn - the output format function to use (colourful-log by default)
    * purge? - purge old log files (false by default)
  "
  [file blacklisted stdout & {:keys [output-fn purge?] :or {output-fn colourful-log purge? false}}]
  (set-config! {:appenders {:println (merge {:ns-whitelist stdout} (println-appender {:stream :auto}))
                            :rolling (rolling-appender {:path (str file ".log") :pattern :weekly})}
                :ns-blacklist blacklisted
                :output-fn (partial output-fn  {:stacktrace-fonts {}}); disable-coloring
                :timestamp-opts {:timezone  (java.util.TimeZone/getDefault)}})
  (when purge? (run-purge (* 60 60 24))))

(defn logstash
  "Add logstash appender"
  [host port]
  (merge-config! {:appenders {:logstash (logstash-appender host port)}}))

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

