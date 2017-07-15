(defproject re-share "0.1.1"
  :description "Common utilities for re-ops"
  :url "https://github.com/re-ops/re-share"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
     [org.clojure/clojure "1.8.0"]

     ; zeromq
     [org.zeromq/jzmq "3.1.1"]

     ; fs access
     [me.raynes/fs "1.4.6"]
     
     ; string interpulation  
     [org.clojure/core.incubator "0.1.4"]
  ]

  :repositories  {"bintray"  "http://dl.bintray.com/content/narkisr/narkisr-jars"}

  :signing {:gpg-key "narkisr@gmail.com"}
)
