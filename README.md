# clj-project

<!-- Uncomment after editing links at the bottom of this page
[![Build Status][gh-actions-badge]][gh-actions] [![Clojars Project][clojars-badge]][clojars] [![Clojure Docs][cljdoc-badge]][cljdoc-link] [![Clojure version][clojure-v]](project.clj)
/-->

A template for starting a clj tools based project.

## Overview

Batteries included features:

- Project dirs/file basics
  - src and test directory structure.
  - .gitignore, build.clj, deps.edn, LICENSE, README.
- CI/CD: Github Workflow that runs tests and packages.
- Dependencies: Dependency version checks with 'antq'.
- Tests: Clojure 'test-runner' from Cognitect.
- Packaging: Create jars/uberjars with 'clojure.tools.build'.
- Deploy: Publish jars/uberjars to clojars via 'deps-deploy'.

## Execute the -main function

Run the -main function in the namespace/core.clj file:

- Method 1: Implied -main function in the namespace. Args sent as a list.

```clojure
clj -M:run-m
```

- Method 2: Explicit namespace+function. Args sent as a key value map.

```clojure
clj -X:run-x
```

## Dependencies

Check for outdated dependencies:

```clojure
clj -M:outdated
```

Upgrade outdated dependencies.

```clojure
clj -M:outdated :upgrade true
```

## Tests

Run tests:

```clojure
clj -T:build test
```

## Packaging

Test, write pom, and build a JAR.

```clojure
clj -T:build jar
```

Test, write pom, and build a uberJAR.

```clojure
clj -T:build uberjar
```

Clean packaging area.

```clojure
clj -T:build clean
```

## Deploy

Deploy jars to clojars:

```clojure
;; env vars for clojars
CLOJARS_USERNAME=username
CLOJARS_PASSWORD=clojars-token

clj -T:build deploy
```

<!-- Named page links below: /-->

[gh-actions-badge]: https://github.com/wdhowe/clj-project/workflows/ci%2Fcd/badge.svg
[gh-actions]: https://github.com/wdhowe/clj-project/actions
[cljdoc-badge]: https://cljdoc.org/badge/com.github.wdhowe/clj-project
[cljdoc-link]: https://cljdoc.org/d/com.github.wdhowe/clj-project/CURRENT
[clojure-v]: https://img.shields.io/badge/clojure-1.11.1-blue.svg
[clojars]: https://clojars.org/com.github.wdhowe/clj-project
[clojars-badge]: https://img.shields.io/clojars/v/com.github.wdhowe/clj-project.svg
