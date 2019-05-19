(ns re-share.encryption
  "pgp secret encryption support, in order to test we can generate a testing keyring:

     gpg --no-default-keyring --keyring trustedkeys.gpg --fingerprint

   A pulic and private keyrings should be exported (for bountycastle support):

     gpg --no-default-keyring --keyring trustedkeys.gpg --export >> test/resources/public.gpg

     gpg --no-default-keyring --keyring trustedkeys.gpg --export-secret-keys >> test/resources/secret.gpg"
  (:require
   [clojure.java.io :as io]
   [clj-pgp.message :as msg]
   [clj-pgp.core :as pgp]
   [clj-pgp.keyring :as keyring])
  (:import java.util.Base64))

(defn load-public [path]
  (keyring/load-public-keyring (io/file path)))

(defn load-private [path]
  (keyring/load-secret-keyring (io/file path)))

(defn list-public [k]
  (keyring/list-public-keys k))

(defn list-private [k]
  (keyring/list-secret-keys k))

(defn- encode [bs]
  (String. (.encode (Base64/getEncoder) bs) "UTF-8"))

(defn- decode [s]
  (.decode (Base64/getDecoder) s))

(defn encrypt
  "Load public key and encrypt input"
  [input public-key]
  (let [pub (first (load-public public-key))]
    (encode (msg/encrypt input pub :format :utf8 :cipher :aes-256 :compress :zip))))

(defn decrypt
  "Unlock secret and decrypt encrypted string (key isn't cached)"
  [encrypted secret-key pass]
  (let [k (first (list-private (load-private secret-key)))
        prv (pgp/unlock-key k pass)]
    (msg/decrypt (decode encrypted) prv)))

(comment
  (def encrypted (encrypt "this is a secret!" "test/resources/public.gpg"))
  (println encrypted)
  (println (decrypt encrypted "test/resources/secret.gpg" "1234")))
