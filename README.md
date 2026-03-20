# clj-project

<!-- Uncomment after editing links at the bottom of this page
[![Build Status][gh-actions-badge]][gh-actions] [![Clojars Project][clojars-badge]][clojars] [![Clojure Docs][cljdoc-badge]][cljdoc-link] [![Clojure version][clojure-v]](deps.edn)
/-->

A template for starting a clj tools based project.

## Overview

Batteries included features:

- Project dirs/file basics
  - src and test directory structure.
  - .gitignore, build.clj, deps.edn, LICENSE, README.
- Tasks: Taskfile for common development tasks.
- Dev: REPL with nREPL and cider-nrepl for Calva/VSCode.
- Quality: Linting with 'clj-kondo', formatting with 'cljfmt'.
- Tests: Clojure 'test-runner' from Cognitect.
- Dependencies: Dependency version checks with 'antq'.
- Packaging: Create jars/uberjars with 'clojure.tools.build'.
- Deploy: Publish jars/uberjars to clojars via 'deps-deploy'.
- Docker: Dockerfile for containerized builds.
- CI/CD: Github Workflow that runs tests and packages.

## Getting Started

After cloning this template, rename the project to your own:

```sh
./scripts/rename-project.sh my-new-project my-github-user "My Name"
```

This replaces all references to the current project name, GitHub username,
and author in source files, configs, and directory names. The GitHub username
and author are optional.

## Usage

Run the -main function in the namespace/core.clj file:

- Method 1: Implied -main function in the namespace. Args sent as a list.

```clojure
clj -M:run-m
```

- Method 2: Explicit namespace+function. Args sent as a key value map.

```clojure
clj -X:run-x
```

## Tasks

A [Taskfile](https://taskfile.dev) is provided as a convenience wrapper around
the clj commands documented below.

List all available tasks:

```sh
task
```

Examples:

```sh
task test           # Run tests
task build          # Build uberjar
task lint           # Lint code
task fmt            # Format code
task docker:build   # Build Docker image
task outdated       # Check for outdated deps
```

## Development

Start a dev REPL with nREPL for Calva/VSCode:

```clojure
clj -M:dev
```

Lint code:

```clojure
clj -M:lint src/ test/
```

Check formatting:

```clojure
clj -M:cljfmt check src/ test/
```

Fix formatting:

```clojure
clj -M:cljfmt fix src/ test/
```

## Tests

Run tests (excludes integration and strict tests):

```clojure
clj -M:test
```

Run tests matching a pattern:

```clojure
clj -X:test-m :p core
```

Run integration tests:

```clojure
clj -X:test-integration
```

Run all tests via build (no exclusions):

```clojure
clj -T:build test
```

## Dependency Maintenance

Check for outdated dependencies:

```clojure
clj -M:outdated
```

Upgrade outdated dependencies.

```clojure
clj -M:outdated --upgrade
```

## Packaging

Build a JAR.

```clojure
clj -T:build jar
```

Build an uberJAR.

```clojure
clj -T:build uber
```

Test, write pom, and build a JAR.

```clojure
clj -T:build ci-jar
```

Test, write pom, and build an uberJAR.

```clojure
clj -T:build ci-uber
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
[clojure-v]: https://img.shields.io/badge/clojure-1.12.4-blue.svg
[clojars]: https://clojars.org/com.github.wdhowe/clj-project
[clojars-badge]: https://img.shields.io/clojars/v/com.github.wdhowe/clj-project.svg
