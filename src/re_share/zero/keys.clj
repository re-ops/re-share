(ns re-share.zero.keys
  (:require
   [clojure.core.strint :refer  (<<)]
   [me.raynes.fs :refer (mkdir exists?)])
  (:import
   [zmq.io.mechanism.curve Curve]
   [java.nio.charset Charset]))

;;  byte [] [] serverKeyPair = new Curve ().keypair ();
;;  byte [] serverPublicKey = serverKeyPair [0];
;;  byte [] serverSecretKey = serverKeyPair [1];

(defn generate-pair
  "Generate pub/secret key pairs"
  [parent prefix]
  (let [pair (.keypairZ85 (Curve.))]
    (spit (str parent "/" prefix "-private.key") (aget pair 1))
    (spit (str parent "/" prefix "-public.key") (aget pair 0))))

(defonce utf8 (Charset/forName "UTF-8"))

(defn paths [parent]
  {:server-public (<< "~{parent}/server-public.key")
   :server-private (<< "~{parent}/server-public.key")
   :client-public (<< "~{parent}/client-public.key")
   :client-private (<< "~{parent}/client-private.key")})

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
    (generate-pair parent "client")))

(defn create-server-keys
  "Lazily create server keys"
  [parent]
  (when-not (server-pub-exist? parent)
    (mkdir parent)
    (generate-pair parent "server")))

(comment
  (create-client-keys ".curve")
  (client-keys-exist? ".curve"))

