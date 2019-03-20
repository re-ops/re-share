(ns re-share.config
  "Configuration handling"
  (:refer-clojure :exclude  [load])
  (:require
   [expound.alpha :as expound]
   [clojure.spec.alpha :as s]
   [aero.core :as aero]
   [clojure.core.strint :refer (<<)]
   [clojure.java.io :refer (file)]))

(s/def ::index string?)

(s/def ::elasticsearch (s/keys :req-un [::index]))

(s/def ::hosts (s/coll-of string?))

(s/def ::cluster (s/keys :req-un [::hosts]))

(s/def :shared/elasticsearch (s/map-of keyword? ::cluster))

(s/def :shared/private-key-path string?)

(s/def :shared/ssh (s/keys :req-un [:shared/private-key-path]))

(s/def ::re-mote (s/keys :req-un [::elasticsearch]))

(s/def ::port integer?)

(s/def ::host string?)

(s/def ::username string?)

(s/def :kvm/node (s/keys :req-un [::username ::host ::port]))

(s/def ::nodes (s/map-of keyword? :kvm/node))

(s/def ::kvm (s/keys :req-un [::nodes]))

(s/def :re-core/hypervisor (s/keys :opt-un [::kvm]))

(s/def ::re-core (s/keys :req-un [::elasticsearch :re-core/hypervisor]))

(s/def ::shared (s/keys :req-un [:shared/elasticsearch :shared/ssh]))

(s/def ::config (s/keys :req-un [::re-mote ::re-core ::shared]))

(def path
  (<< "~(System/getProperty \"user.home\")/.re-ops.edn"))

(def ^{:private true :dynamic true} profile {:profile :dev})

(def config
  (let [c (aero/read-config path profile)]
    (if-not (s/valid? ::config c)
      (expound/expound ::config c)
      c)))

(defn get!
  "Reading a keys path from configuration raises an error of keys not found"
  [& ks]
  (if-let [v (get-in config ks)]
    v
    (throw (ex-info (<< "No matching configuration keys ~{ks} found") {:keys ks :type ::missing-conf}))))

(defn get*
  "nil on missing version of get!"
  [& keys]
  (get-in config keys))
