(ns build
  "Clean:
   clj -T:build clean

   Test:
   clj -T:build test

   Test, Write POM, Build JAR:
   clj -T:build jar

   Test, Write POM, Build UberJAR:
   clj -T:build uberjar

   Deploy to Clojars:
   CLOJARS_USERNAME=username
   CLOJARS_PASSWORD=clojars-token
   clj -T:build deploy"
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def project
  "Project Metadata"
  {:description "A template for starting a clj deps/tools based project."
   :lib 'com.github.wdhowe/clj-project
   :main 'clj-project.core
   :version "1.0.0"
   :license "Eclipse Public License"
   :license-url "http://www.eclipse.org/legal/epl-v20.html"
   :author "Bill Howe"
   :url "https://github.com/wdhowe/clj-project"
   :git-repo "wdhowe/clj-project"
   :class-dir "target/classes"})

(defn- pom-template
  [version]
  (let [git-repo (:git-repo project)
        url (:url project)]
   [[:description (:description project)]
    [:url url]
    [:licenses
     [:license
      [:name (:license project)]
      [:url (:license-url project)]]]
    [:developers
     [:developer
      [:name (:author project)]]]
    [:scm
     [:url url]
     [:connection (format "scm:git:%s.git" url)]
     [:developerConnection (format "scm:git:ssh:git@github.com:%s.git" git-repo)]
     [:tag (str "v" version)]]]))

(defn- jar-opts
  [opts]
  (let [version (:version project)
        lib     (:lib project)]
    (assoc opts
           :basis     (b/create-basis {})
           :class-dir (:class-dir project)
           :jar-file  (format "target/%s-%s.jar" lib version)
           :lib       lib
           :pom-data  (pom-template version)
           :src-dirs  ["src"]
           :target    "target"
           :version   version)))

(defn- uber-opts [opts]
  (let [version (:version project)
        lib     (:lib project)]
  (assoc opts
         :basis      (b/create-basis {})
         :class-dir  (:class-dir project)
         :lib        lib
         :main       (:main project)
         :ns-compile [(:main project)]
         :pom-data   (pom-template version)
         :src-dirs   ["src"]
         :uber-file  (format "target/%s-%s-standalone.jar" lib version)
         :version    version)))

(defn clean
  "Remove build directory."
  [_]
  (b/delete {:path "target"}))

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

(defn jar
  "Run the CI pipeline of: tests, write pom, and build the JAR."
  [opts]
  (test opts)
  (clean nil)
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"]
                 :target-dir (:class-dir project)})
    (println "\nBuilding JAR" (:jar-file opts) "...")
    (b/jar opts)
    (println (format "Created %s" (:jar-file opts))))
  opts)

(defn uberjar
  "Run the CI pipeline of tests (and build the uberjar)."
  [opts]
  (test opts)
  (clean nil)
  (let [opts (uber-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"]
                 :target-dir (:class-dir project)})
    (println (str "\nCompiling " (:main project) "..."))
    (b/compile-clj opts)
    (println "\nBuilding UberJAR...")
    (b/uber opts)
    (println (format "Created %s" (:uber-file opts))))
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
