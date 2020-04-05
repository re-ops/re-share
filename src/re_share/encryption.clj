(ns re-share.encryption
  "pgp secret encryption support, in order to test we can generate a testing keyring:

     gpg --no-default-keyring --keyring trustedkeys.gpg --fingerprint

     gpg --no-default-keyring --keyring trustedkeys.gpg --gen-key

   A public and private keyrings should be exported (for bountycastle support):

     gpg --no-default-keyring --keyring trustedkeys.gpg --export >> test/resources/public.gpg

     gpg --no-default-keyring --keyring trustedkeys.gpg --export-secret-keys >> test/resources/secret.gpg"
  (:require
   [clojure.java.io :as io :refer (delete-file)]
   [clojure.core.strint :refer  (<<)]
   [clojure.java.shell :refer (sh)]
   [re-share.wait :refer (wait-for)]
   [clj-pgp.message :as msg]
   [clj-pgp.core :as pgp]
   [clj-pgp.keyring :as keyring]
   [taoensso.timbre :refer  (refer-timbre)])
  (:import java.util.Base64))

(refer-timbre)

(defn load-public [path]
  (keyring/load-public-keyring (io/file path)))

(defn load-private [path]
  (keyring/load-secret-keyring (io/file path)))

(defn list-public [k]
  (keyring/list-public-keys k))

(defn list-private [k]
  (keyring/list-secret-keys k))

(defn encode [bs]
  (String. (.encode (Base64/getEncoder) bs) "UTF-8"))

(defn decode [s]
  (.decode (Base64/getDecoder) s))

(defn encrypt
  "Load public key and encrypt input"
  [input public-key]
  (let [pub (first (load-public public-key))]
    (msg/encrypt input pub :format :utf8 :cipher :aes-256 :compress :zip)))

(defn decrypt
  "Unlock secret and decrypt encrypted string (key isn't cached)"
  [encrypted secret-key pass]
  (let [k (first (list-private (load-private secret-key)))
        prv (pgp/unlock-key k pass)]
    (msg/decrypt encrypted prv)))

(def alpha-numeric
  (map char (concat (range 48 58) (range 66 91) (range 97 123))))

(defn random-str  [l]
  (apply str
         (map (fn [_] (rand-nth alpha-numeric)) (range l))))

(defn read-pass
  "Under both lein run and lein repl (System/console) is nil, we use following workaround (works only under tmux):
      1. Create a tmux window and use read -s to write the password into a file.
      2. Read the file and delete it.
   Note:
     1. This isn't as secure as System/console readPassword but still better than reading the password in the clear.
     2. We assume that we run under tmux (no check is made)
    "
  []
  (let [f (<< "/tmp/~(random-str 10)")]
    (try
      (sh "tmux" "split-window" (<< "read -s pass && echo ${pass} >> ~{f}"))
      (wait-for {:timeout [10000 :ms] :sleep [500 :ms]}
                #(try
                   (not-empty (slurp f))
                   (catch Exception e false))
                "Timed out while reading the password")
      (slurp f)
      (finally
        (try
          (delete-file f)
          (catch Exception e
            (error (<< "Failed to delete ~{f} temporary password file! please make sure to delete it."))
            (System/exit 1)))))))

(comment
  (def encrypted (encrypt "this is a secret!" "test/resources/public.gpg"))

  (println encrypted)
  (println (decrypt encrypted "test/resources/secret.gpg" "1234")))
