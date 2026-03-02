(ns test-runner
  "Convenience wrapper for running tests with simpler CLI syntax."
  (:require [cognitect.test-runner.api :as tr]))

(defn match
  "Run tests matching pattern substring.

   Usage:
     clj -X:test-m :p reflection
     clj -X:test-m :p reflection :includes '[:strict]'

   Args:
     opts: Map with :p (pattern substring to match).
           Optional :includes/:excludes to override defaults.

   Returns:
     Test results."
  [{:keys [p] :as opts}]
  (let [test-opts (dissoc opts :p)
        defaults (when-not (:includes test-opts)
                   {:excludes [:integration :strict]})]
    (tr/test (merge defaults
                    test-opts
                    {:patterns [(str ".*" p ".*")]}))))
