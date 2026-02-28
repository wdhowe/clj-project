(ns build
  "Clean:
   clj -T:build clean

   Test:
   clj -T:build test

   Test, Write POM, Build JAR:
   clj -T:build jar

   Build UberJAR:
   clj -T:build uber

   Test, Write POM, Build UberJAR:
   clj -T:build ci

   Deploy to Clojars:
   CLOJARS_USERNAME=username
   CLOJARS_PASSWORD=clojars-token
   clj -T:build deploy"
  (:refer-clojure :exclude [test])
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

;;; Project Configuration ;;;

(defn- git-version
  "Derive version from git tags.

   Returns:
     Version string, or nil if no matching tag found."
  []
  (try
    (let [{:keys [exit out]} (shell/sh "git" "describe" "--tags" "--match" "v*" "--always")]
      (when (zero? exit)
        (-> out string/trim (string/replace-first #"^v" ""))))
    (catch Exception _ nil)))

(def version
  "Project version.

   Derived from git tags with fallback to resources/VERSION."
  (or (git-version)
      (-> (slurp "resources/VERSION") string/trim)))

(def project
  "Project metadata."
  {:lib 'com.github.wdhowe/clj-project
   :version version
   :description "A template for starting a clj deps/tools based project."
   :main 'clj-project.core
   :author "Bill Howe"
   :url "https://github.com/wdhowe/clj-project"
   :git-repo "wdhowe/clj-project"
   :license "Eclipse Public License"
   :license-url "http://www.eclipse.org/legal/epl-v20.html"
   :src-dirs ["src"]
   :resource-dirs ["resources"]
   :target-dir "target"
   :class-dir "target/classes"})

(defn- pom-template
  []
  [[:description (:description project)]
   [:url (:url project)]
   [:licenses
    [:license
     [:name (:license project)]
     [:url (:license-url project)]]]
   [:developers
    [:developer
     [:name (:author project)]]]
   [:scm
    [:url (:url project)]
    [:connection (format "scm:git:%s.git" (:url project))]
    [:developerConnection (format "scm:git:ssh:git@github.com:%s.git" (:git-repo project))]
    [:tag (str "v" (:version project))]]])

(defn- jar-opts
  [opts]
  (assoc opts
         :lib       (:lib project)
         :version   (:version project)
         :basis     (b/create-basis {})
         :jar-file  (format "%s/%s-%s.jar" (:target-dir project) (:lib project) (:version project))
         :pom-data  (pom-template)
         :src-dirs  (:src-dirs project)
         :class-dir (:class-dir project)
         :target    (:target-dir project)))

(defn- uber-opts [opts]
  (assoc opts
         :lib (:lib project)
         :version (:version project)
         :main (:main project)
         :basis (b/create-basis {})
         :ns-compile [(:main project)]
         :pom-data (pom-template)
         :uber-file (format "%s/%s-%s.jar" (:target-dir project) (:lib project) (:version project))
         :src-dirs (:src-dirs project)
         :class-dir (:class-dir project)))

;;; Build Tasks ;;;

(defn clean
  "Clean build artifacts.

   Args:
     opts: Build options map.

   Returns:
     opts."
  [opts]
  (println "Cleaning build artifacts...")
  (b/delete {:path (:target-dir project)})
  opts)

(defn test
  "Run all the tests.

   Args:
     opts: Build options map.

   Returns:
     opts.

   Throws:
     ExceptionInfo if tests fail."
  [opts]
  (let [basis (b/create-basis {:aliases [:test]})
        cmds (b/java-command
              {:basis     basis
               :main      'clojure.main
               :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn jar
  "Run the CI pipeline of tests, write pom, and build the JAR.

   Args:
     opts: Build options map.

   Returns:
     opts."
  [opts]
  (test opts)
  (clean opts)
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs (into (:resource-dirs project) (:src-dirs project))
                 :target-dir (:class-dir project)})
    (println "\nBuilding JAR" (:jar-file opts) "...")
    (b/jar opts)
    (println (format "Created %s" (:jar-file opts))))
  opts)

(defn- build-uberjar
  "Build uberjar from resolved opts.

   Copies source, writes VERSION, compiles, and packages.

   Args:
     opts: Resolved uber-opts map.

   Returns:
     opts."
  [opts]
  (println (str "Version: " version))
  (println "Copying source...")
  (b/copy-dir {:src-dirs (into (:resource-dirs project) (:src-dirs project))
               :target-dir (:class-dir project)})
  (spit (str (:class-dir project) "/VERSION") version)
  (println (str "Compiling " (:main project) "..."))
  (b/compile-clj opts)
  (println (str "Building UberJAR... " (:uber-file opts)))
  (b/uber opts)
  (println (format "Created %s" (:uber-file opts)))
  opts)

(defn uber
  "Build the uberjar.

   Args:
     opts: Build options map.

   Returns:
     opts."
  [opts]
  (clean opts)
  (build-uberjar (uber-opts opts)))

(defn ci
  "Run the CI pipeline of tests, write pom, and build the uberjar.

   Args:
     opts: Build options map.

   Returns:
     opts."
  [opts]
  (test opts)
  (clean opts)
  (let [opts (uber-opts opts)]
    (println "Writing pom.xml...")
    (b/write-pom opts)
    (build-uberjar opts)))

(defn deploy
  "Deploy the JAR to Clojars.

   Args:
     opts: Build options map.

   Returns:
     opts."
  [opts]
  (let [{:keys [jar-file] :as opts}
        (jar-opts opts)]
    (dd/deploy {:installer :remote
                :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
