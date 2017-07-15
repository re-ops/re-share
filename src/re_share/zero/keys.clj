(ns re-share.zero.keys
 (:require
   [clojure.core.strint :refer  (<<)]
   [me.raynes.fs :refer (mkdir exists?)])
 (:import
   [org.zeromq ZCert ZContext ZAuth]
   [java.nio.charset Charset]))

(defn- setup
  "Setup auth context"
  []
  (doto (ZAuth. (ZContext.))
   (.setVerbose true)))

(defn generate-pair
  "Generate pub/secret key pairs"
  [parent prefix]
  (let [zcert (ZCert.)]
    (spit (str parent "/" prefix "-private.key") (.getSecretKeyAsZ85 zcert) )
    (spit (str parent "/" prefix "-public.key") (.getPublicKeyAsZ85 zcert) )))

(defonce utf8 (Charset/forName "UTF-8"))

(defn paths [parent]
  {:server-public (<< "~{parent}/server-public.key")
   :server-private (<< "~{parent}/server-public.key")
   :client-public (<< "~{parent}/client-public.key")
   :client-private (<< "~{parent}/client-private.key")
   })

(defn read-key [k]
  {:post [(= (alength %) 40)]}
  (.getBytes (slurp k) utf8))

(defn check-keys [parent ks]
  (try
     (let [missing (first (filter (fn [[_ v]] (not (and (exists? v) (read-key v)))) ks))]
       (and (exists? parent) (empty? missing)))
     (catch java.lang.AssertionError e false)))

(defn client-keys-exist?
   "Check client keys are in place"
   [parent]
   (check-keys parent (select-keys (paths parent) [:client-public :client-private])))

(defn server-pub-exist?
   "Check server keys are in place"
   [parent]
   (check-keys parent (select-keys (paths parent) [:server-public])))

(defn server-keys-exist?
   "Check server keys are in place"
   [parent]
   (check-keys parent (select-keys (paths parent) [:server-public :server-private])))

(defn create-client-keys
   "Lazily create client keys and copy server public key"
   [parent]
  (when-not (client-keys-exist? parent)
    (mkdir parent)
    (setup)
    (generate-pair parent "client")))

(defn create-server-keys
   "Lazily create server keys"
   [parent]
  (when-not (server-pub-exist? parent)
    (mkdir parent)
    (setup)
    (generate-pair parent "server")))

(comment
  (create-keys ".curve")
  (client-keys-exist? ".curve")
  )

