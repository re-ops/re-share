(ns re-share.core)

(defn find-port
   "find the first available port within a given range"
   [from to]
   (first
     (filter
       (fn [p] (try (with-open [s (java.net.ServerSocket. p)] true) (catch Exception e false))) (range from to))))


