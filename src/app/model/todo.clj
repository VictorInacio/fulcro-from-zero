(ns app.model.todo
  (:require [com.wsscode.pathom.connect :as pc]
            [app.db :as db]))

(pc/defresolver all-todos-resolver [env {:todo-item/keys [id]}]
  {::pc/output [{:all-todos [:todo-item/id]}]}
  {:all-todos (->> (db/q '[:find (pull ?e [:todo-item/id])
                           :where [?e :todo-item/id]]
                         (db/get-db))
                   (mapv first))})

(pc/defresolver todo-resolver [env {:todo-item/keys [id]}]
  {::pc/input  #{:todo-item/id}
   ::pc/output [:todo-item/id :todo-item/title :todo-item/completed]}
  (ffirst (db/q '[:find (pull ?e [:todo-item/id :todo-item/title :todo-item/completed])
                  :in $ ?todo-id
                  :where [?e :todo-item/id ?todo-id]]
                (db/get-db) id)))

(def resolvers [todo-resolver all-todos-resolver])


(comment
  (db/q '[:find ?e
          :where [?e :todo-item/id 1]]
        (db/get-db))
  (db/q '[:find (pull ?e [:todo-item/id :todo-item/title :todo-item/completed])
          :in $ ?todo-id
          :where [?e :todo-item/id ?todo-id]]
        (db/get-db) 1)
  )