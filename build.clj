(ns build
  "build script.
  
   Test
   clj -T:build test

   Test, Write POM, Build
   clj -T:build ci

   Deploy to Clojars
   CLOJARS_USERNAME=username CLOJARS_PASSWORD=clojars-token clj -T:build deploy"
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def project
  "Project Metadata"
  {:lib (symbol "com.github.wdhowe/clj-project") 
   :version "1.0.0"
   :author "Bill Howe"
   :username "wdhowe" 
   :git-repo "wdhowe/clj-project"})

(defn- the-version [patch] (format "1.0.%s" patch))
(def snapshot (the-version "999-SNAPSHOT"))
(def class-dir "target/classes")

(defn test
  "Run all the tests."
  [opts]
  (let [basis (b/create-basis {:aliases [:test]})
        cmds  (b/java-command
                {:basis     basis
                 :main      'clojure.main
                 :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn- pom-template
  [version]
  [[:description "A library of functions to enhance clojure.core."]
   [:url (format "https://github.com/%s" (:git-repo project))]
   [:licenses
    [:license
     [:name "Eclipse Public License"]
     [:url "http://www.eclipse.org/legal/epl-v20.html"]]]
   [:developers
    [:developer
     [:name (:author project)]]]
   [:scm
    [:url (format "https://github.com/%s" (:git-repo project))]
    [:connection (format "scm:git:https://github.com/%s.git" (:git-repo project))]
    [:developerConnection (format "scm:git:ssh:git@github.com:%s.git" (:git-repo project))]
    [:tag (str "v" version)]]])

(defn- jar-opts
  [opts]
  (let [version (if (:snapshot opts) snapshot (:version project))]
    (assoc opts
           :lib (:lib project)
           :version version
           :jar-file  (format "target/%s-%s.jar" (:lib project) version)
           :basis     (b/create-basis {})
           :class-dir class-dir
           :target    "target"
           :src-dirs  ["src"]
           :pom-data  (pom-template version))))

(defn ci
  "Run the CI pipeline of: tests, write pom, and build the JAR."
  [opts]
  (test opts)
  (b/delete {:path "target"})
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println "\nBuilding" (:jar-file opts) "...")
    (b/jar opts))
  opts)

(defn deploy
  "Deploy the JAR to Clojars."
  [opts]
  (let [{:keys [jar-file] :as opts}
        (jar-opts opts)]
    (dd/deploy {:installer :remote
                :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
