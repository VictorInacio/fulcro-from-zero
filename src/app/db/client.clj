(ns app.db.client
  (:require [datomic.client.api :as d]))

(def DB-NAME {:db-name "todo-db"})

(def client (d/client {:server-type :datomic-local
                       :system      "dev"}))
(comment
  (d/list-databases client {})
  (d/delete-database client {:db-name "todo-db"})
  (d/create-database client {:db-name "todo-db"})
  )

(defn connection []
  (d/connect client DB-NAME))

(defn get-db []
  (d/db (connection)))

(defn transact [tx-data]
  (d/transact (d/connect client DB-NAME) {:tx-data tx-data}))

(def q d/q)
(def pull d/pull)

