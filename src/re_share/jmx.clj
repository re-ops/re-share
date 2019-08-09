(ns re-share.jmx
  (:require
   [clojure.java.jmx :as jmx]))

(defn memory-use []
  (jmx/mbean "java.lang:type=Memory"))

(defn list-beans []
  (jmx/mbean-names "*:*"))

(comment)
