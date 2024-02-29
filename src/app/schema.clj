(ns app.schema
  (:require [app.db :as db]))

(def todo-schema-tx-data [{:db/ident       :todo-item/id
                           :db/valueType   :db.type/long
                           :db/unique      :db.unique/identity
                           :db/cardinality :db.cardinality/one
                           :db/doc         "Todo item id"}

                          {:db/ident       :todo-item/title
                           :db/valueType   :db.type/string
                           :db/cardinality :db.cardinality/one
                           :db/doc         "Todo item title"}

                          {:db/ident       :todo-item/completed
                           :db/valueType   :db.type/boolean
                           :db/cardinality :db.cardinality/one
                           :db/doc         "Todo item completion"}])

(comment
  squuid
  (db/transact todo-schema-tx-data)

  (def sample-todo {:todo-item/id    1
                    :todo-item/title "Hello todo"})

  (db/transact [sample-todo])

  (def complete-todo {:todo-item/id        1
                      :todo-item/completed true})

  (db/transact [complete-todo])

  (def todo-id (ffirst (db/q '[:find ?e
                               :where [?e :todo-item/id 1]]
                             (db/get-db))))

  (db/pull (db/get-db) '[*] todo-id)

  (db/q '[:find (max ?id)
          :where
          [?e :todo-item/id ?id]] (db/get-db))
  )
