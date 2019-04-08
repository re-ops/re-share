(ns re-share.config
  "Configuration handling"
  (:refer-clojure :exclude  [load])
  (:require
   [re-share.spec :as re-ops]
   [expound.alpha :as expound]
   [clojure.spec.alpha :as s]
   [aero.core :as aero]
   [clojure.core.strint :refer (<<)]
   [clojure.java.io :refer (file)]))

(s/def ::index string?)

(s/def ::elasticsearch (s/keys :req-un [::index]))

(s/def ::hosts (s/coll-of :re-ops/host))

(s/def ::cluster (s/keys :req-un [::hosts]))

(s/def :shared/elasticsearch (s/map-of keyword? ::cluster))

(s/def :shared/private-key-path string?)

(s/def :shared/ssh (s/keys :req-un [:shared/private-key-path]))

(s/def ::re-mote (s/keys :req-un [::elasticsearch]))

(s/def ::username string?)

(s/def :kvm/node (s/keys :req-un [::username :re-ops/host :re-ops/port]))

(s/def :kvm/nodes (s/map-of keyword? :kvm/node))

(s/def :re-core/kvm (s/keys :req-un [:kvm/nodes]))

(s/def :lxc/node (s/keys :req-un [:re-ops/host :re-ops/port]))

(s/def :lxc/nodes (s/map-of keyword? :lxc/node))

(s/def :re-core/lxc (s/keys :req-un [:lxc/nodes]))

(s/def :re-core/hypervisor (s/keys :opt-un [:re-core/kvm :re-core/lxc]))

(s/def :re-core/queue-dir string?)

(s/def ::re-core (s/keys :req-un [::elasticsearch :re-core/hypervisor :re-core/queue-dir]))

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
