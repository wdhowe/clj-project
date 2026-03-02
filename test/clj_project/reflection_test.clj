(ns clj-project.reflection-test
  "Tests for detecting reflection warnings in the codebase.

   Usage:
     clj -X:test-m :p reflection
       Lenient - prints warnings but passes

     clj -X:test-m :p reflection :includes '[:strict]'
       Strict - fails on any warnings"
  (:require [clojure.test :refer [deftest is testing]])
  (:import [java.io PrintWriter StringWriter]))

(defn- collect-reflection-warnings
  "Reload all namespaces with *warn-on-reflection* and capture warnings.

   Returns a string containing any reflection warnings."
  []
  (let [warnings (StringWriter.)]
    (binding [*warn-on-reflection* true
              *err* (PrintWriter. warnings)]
      (require 'clj-project.core :reload-all))
    (str warnings)))

(deftest check-reflection-warnings
  (testing "Compile all namespaces and print any reflection warnings"
    (let [warnings (collect-reflection-warnings)]
      (when (seq warnings)
        (println "Reflection warnings:\n" warnings)))))

(deftest ^:strict check-reflection-warnings-strict
  (testing "Compile all namespaces and fail if reflection warnings exist"
    (let [warnings (collect-reflection-warnings)]
      (is (empty? warnings)
          (str "Reflection warnings found:\n" warnings)))))
