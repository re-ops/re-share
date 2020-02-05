(ns user
  (:require
   [re-share.log :refer (debug-on debug-off setup)]
   [clojure.java.io :as io]
   [clojure.repl :refer :all]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]))

(def system nil)

(defn init
  "Constructs the current development system."
  []
  (setup "re-share" ["oshi.*"] []))

(defn start
  "Starts the current development system."
  [])

(defn stop
  "Shuts down and destroys the current development system."
  [])

(declare go)

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn clear
  "clean repl"
  []
  (print (str (char 27) "[2J"))
  (print (str (char 27) "[;H")))
