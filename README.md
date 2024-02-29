# TODO APP

## Architecture

- Datomic Local
- Pathom Parser
- HTTP Server
- Fulcro SPA

## Schema
Todo Item containing an id, title and completed boolean.

## Run
Datomic:
Add on your home folder the dir `.datomic` and add the file `local.edn` with:
```clojure
{:storage-dir "/Users/victorinacio/.datomic/data"}
```

Start shadow watch:
```shell
shadow-cljs watch :main
```

Start a local repl with dev alias enabled `-A:dev`:
```clojure
;; on dev/user.clj namespace run:
(start)
```