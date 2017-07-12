(defproject re-com "0.1.0"
  :description "Common utilities for re-ops"
  :url "https://github.com/re-ops/re-com"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
     [org.clojure/clojure "1.8.0"]


     ; zeromq
     [org.zeromq/jzmq "3.1.1-SNAPSHOT"]

     ; fs access
     [me.raynes/fs "1.4.6"]
     
     ; string interpulation  
     [org.clojure/core.incubator "0.1.4"]
  ]

  :signing {:gpg-key "narkisr@gmail.com"}
)
