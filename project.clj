(defproject re-share "0.2.3"
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

     ; logging
     [com.taoensso/timbre "4.10.0"]

     ; timeunits
     [fogus/minderbinder "0.2.0"]

     ; metrics
     [com.github.oshi/oshi-core "3.4.3" :exclude [net.java.dev.jna/jna net.java.dev.jna/jna-platform]]
     [com.github.oshi/oshi-json "3.4.3"]
     [net.java.dev.jna/jna "4.5.0"]
     [net.java.dev.jna/jna-platform "4.5.0"]

     [cheshire "5.7.1"]
  ]

   :plugins [
     [jonase/eastwood "0.2.4"]
     [lein-cljfmt "0.5.6"]
     [lein-ancient "0.6.7" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"]
     [lein-set-version "0.3.0"]]

   :aliases {
     "travis" [
        "do" "clean," "compile," "cljfmt" "check," "eastwood"
     ]
   }

  :repositories  {"bintray"  "http://dl.bintray.com/content/narkisr/narkisr-jars"}

  :signing {:gpg-key "narkisr@gmail.com"}
)
