(defproject re-share "0.17.1"
  :description "Common utilities for re-ops"
  :url "https://github.com/re-ops/re-share"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
     [org.clojure/clojure "1.10.3"]

     ; zeromq
     [org.zeromq/jeromq "0.5.2"]

     ; fs access
     [me.raynes/fs "1.4.6"]

     ; string interpulation
     [org.clojure/core.incubator "0.1.4"]

     ; logging
     [com.taoensso/timbre "5.1.0"]
     [com.fzakaria/slf4j-timbre "0.3.20"]
     [timbre-ns-pattern-level "0.1.2"]

     ; timeunits
     [fogus/minderbinder "0.3.0"]

     ; metrics
     [com.github.oshi/oshi-core "5.3.6"]
     [org.clojure/java.data "1.0.86"]

     ; serialization
     [cheshire "5.10.0"]

     ; scheduling
     [org.clojure/core.async "1.3.610"]
     [jarohen/chime "0.3.2" :exclusions [org.clojure/core.async]]

     ; pretty output
     [narkisr/clansi "1.2.0"]

     ; Elasticsearch
     [rubber "0.4.1"]

     ; configuration
     [aero "1.1.6"]

     ; encryption
     [mvxcvi/clj-pgp "1.0.0"]

     ; to be removed
     [clj-time "0.15.2"]
   ]

   :plugins [
     [lein-cljfmt "0.5.6"]
     [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"]
     [lein-set-version "0.3.0"]]

   :profiles {
     :dev {
       :dependencies [
         [org.clojure/tools.namespace "1.1.0"]
       ]
     }
     :codox {
       :dependencies [
          [org.clojure/tools.reader "1.3.4"]
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

  :main re-share.config.secret
)
