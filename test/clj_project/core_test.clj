(ns clj-project.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [clj-project.core :as core]))

(deftest -main-test
  (testing "Fixme, I ALWAYS pass."
    (core/-main "arg1" "arg2")
    (is (= true true))))

(deftest ^:integration example-integration-test
  (testing "Example integration test that should pass."
    (is (= 1 1))))
