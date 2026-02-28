(ns test-runner
  "Convenience wrapper for running tests with simpler CLI syntax."
  (:require [cognitect.test-runner.api :as tr]))

(defn match
  "Run tests matching pattern substring.
   Usage: clj -X:test-m :p reflection

   Parameters:
   - opts - map with :p (pattern substring to match)

   Returns test results."
  [{:keys [p]}]
  (tr/test {:patterns [(str ".*" p ".*")]
            :excludes [:integration]}))
