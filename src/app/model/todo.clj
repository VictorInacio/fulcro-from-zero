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

(defn insert-todo-item [db connection title]
  (let [last-id  (or (ffirst (d/q '[:find (max ?id)
                                    :where
                                    [?e :todo-item/id ?id]] db)) 0)
        curr-id  (inc last-id)
        new-todo {:todo-item/id        curr-id
                  :todo-item/title     title
                  :todo-item/completed false}]
    (d/transact connection {:tx-data [new-todo]})
    {:tx-result new-todo}))


(pc/defmutation add-todo [{:keys [db connection]} {:todo-item/keys [title]}]
  {::pc/sym `app.model.todo/add-todo
   ::pc/output [:todo-item/result]}
  (insert-todo-item db connection title)
  {:todo-item/result "OK"})

(def resolvers [todo-resolver all-todos-resolver add-todo])
