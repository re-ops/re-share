(defproject re-share "0.15.0"
  :description "Common utilities for re-ops"
  :url "https://github.com/re-ops/re-share"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
     [org.clojure/clojure "1.10.1"]

     ; zeromq
     [org.zeromq/jeromq "0.5.1"]

     ; fs access
     [me.raynes/fs "1.4.6"]

     ; string interpulation
     [org.clojure/core.incubator "0.1.4"]

     ; logging
     [com.taoensso/timbre "4.10.0"]
     [com.fzakaria/slf4j-timbre "0.3.8"]
     [timbre-ns-pattern-level "0.1.2"]

     ; timeunits
     [fogus/minderbinder "0.3.0"]

     ; metrics
     [com.github.oshi/oshi-core "4.3.0"]
     [org.clojure/java.data "0.2.0"]

     ; serialization
     [cheshire "5.7.1"]

     ; scheduling
     [clj-time/clj-time "0.14.2"]
     [org.clojure/core.async "0.3.443"]
     [jarohen/chime "0.2.1" :exclusions [org.clojure/core.async]]

     ; pretty output
     [narkisr/clansi "1.2.0"]

     ; Elasticsearch
     [rubber "0.3.7"]

     ; configuration
     [aero "1.1.3"]
     [expound "0.7.2"]

     ; encryption
     [mvxcvi/clj-pgp "0.10.0"]
   ]

   :plugins [
     [lein-cljfmt "0.5.6"]
     [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"]
     [lein-set-version "0.3.0"]]

   :profiles {
     :dev {
       :dependencies [
         [org.clojure/tools.namespace "0.2.11"]
       ]
     }
     :codox {
       :dependencies [
          [org.clojure/tools.reader "1.1.0"]
          [codox-theme-rdash "0.1.2"]]
         :plugins [[lein-codox "0.10.3"]]
         :codox {
            :project {:name "re-share"}
            :themes [:rdash]
            :source-paths ["src"]
            :source-uri "https://github.com/re-ops/re-share/blob/master/{filepath}#L{line}"
         }
      }
   }


   :aliases {
     "travis" [
        "do" "clean," "compile," "cljfmt" "check"
     ]
     "docs" [
         "with-profile" "codox" "do" "codox"
     ]
   }

  :repositories  {"bintray"  "https://dl.bintray.com/content/narkisr/narkisr-jars"}

  :main re-share.config.secret
)
