(ns re-share.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def :re-ops/port (s/int-in 1 65536))

(s/def :re-ops/password string?)

(s/def :re-ops/host string?)

(s/def :re-ops/path string?)


