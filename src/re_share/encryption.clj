(ns re-share.encryption
  (:require
   [clojure.java.io :as io]
   (clj-pgp
    [core :as pgp]
    [keyring :as keyring]
    [generate :as gen]))
  (:import java.util.Base64))

(def k (keyring/load-public-keyring (io/file "/home/ronen/.gnupg/trustdb.gpg")))

(keyring/list-public-keys k)

(def rsa-gen (gen/rsa-keypair-generator 2048))

(def sign-gen (gen/signature-generator :master))

(def rsa-pair (gen/generate-keypair rsa-gen :rsa-general))

(def kz (gen/keyring-generator "ronen" "1234" rsa-pair sign-gen))

(def ring (gen/generate-keyrings kz))

(defn encode [to-encode]
  (.encodeToString (Base64/getEncoder) to-encode))

(comment
  (encode (pgp/encode (:public ring)))
  (def foo (pgp/hex-id (first (keyring/list-secret-keys (:secret ring)))))
  (pgp/unlock-key (keyring/get-secret-key (:secret ring) foo) "1234"))
