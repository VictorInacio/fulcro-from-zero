(ns app.model.todo
  (:require [com.wsscode.pathom.connect :as pc]
            [datomic.client.api :as d]))

(pc/defresolver all-todos-resolver [{:keys [db]} {:todo-item/keys [id]}]
  {::pc/output [{:all-todos [:todo-item/id]}]}
  {:all-todos (->> (d/q '[:find (pull ?e [:todo-item/id])
                           :where [?e :todo-item/id]]
                         db)
                   (mapv first))})

(pc/defresolver todo-resolver [{:keys [db]} {:todo-item/keys [id]}]
  {::pc/input  #{:todo-item/id}
   ::pc/output [:todo-item/id :todo-item/title :todo-item/completed]}
  (ffirst (d/q '[:find (pull ?e [:todo-item/id :todo-item/title :todo-item/completed])
                  :in $ ?todo-id
                  :where [?e :todo-item/id ?todo-id]]
                db id)))

(def resolvers [todo-resolver all-todos-resolver])
