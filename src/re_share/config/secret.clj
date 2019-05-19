(ns re-share.config.secret
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [re-share.encryption :as enc]))

(defn read-password
  "Won't work in the repl because (. System console) is null"
  []
  {:pre [(not (nil? (. System console)))]}
  (apply str (.readPassword (. System console) "Please enter secret store password:")))

(defn load-secrets
  "Load secret source file from encrypted store and output into /tmp/secrets.edn
   This cannot be invoked in the repl due to the lack of System console."
  [source prv]
  (let [pass (read-password)]
    (enc/decrypt source prv pass)))

(defn save-secrets
  "Save /tmp/secrets.edn into an encrypted file"
  [target pub]
  (let [input (slurp (io/file "/tmp/secrets.edn"))
        output (enc/encrypt input pub)]
    (spit target output)))

(defn -main [& args]
  (let [[source prv] args]
    (load-secrets source prv)))

(comment
  (save-secrets "secrets" "test/resources/public.gpg")
  (load-secrets "secrets" "test/resources/secret.gpg"))
