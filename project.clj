(defproject re-share "0.2.0"
  :description "Common utilities for re-ops"
  :url "https://github.com/re-ops/re-share"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
     [org.clojure/clojure "1.8.0"]

     ; zeromq
     [org.zeromq/jeromq "0.4.1"]

     ; fs access
     [me.raynes/fs "1.4.6"]

     ; string interpulation
     [org.clojure/core.incubator "0.1.4"]

     ; timeunits
     [fogus/minderbinder "0.2.0"]

     ; metrics
     [com.github.oshi/oshi-core "3.4.2"]
     [com.github.oshi/oshi-json "3.4.2"]
     [cheshire "5.7.1"]

  ]

  :repositories  {"bintray"  "http://dl.bintray.com/content/narkisr/narkisr-jars"}

  :signing {:gpg-key "narkisr@gmail.com"}
)
