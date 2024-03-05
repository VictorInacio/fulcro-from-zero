(ns app.model.todo
  (:require [com.wsscode.pathom.connect :as pc]
            [datomic.client.api :as d]))

(pc/defresolver all-todos-resolver [{:keys [db] :as env} {}]
  {::pc/output [{:all-todos [:todo-item/id :todo-item/title :todo-item/completed]}]}
  (let []
    (tap> env)
    {:all-todos (flatten (d/q '[:find (pull ?e [*])
                                :where [?e :todo-item/id]]
                              db))}))

(pc/defresolver todo-resolver [{:keys [db]} {:todo-item/keys [id]}]
  {::pc/input  #{:todo-item/id}
   ::pc/output [:todo-item/id :todo-item/title :todo-item/completed]}
  (ffirst (d/q '[:find (pull ?e [:todo-item/id :todo-item/title :todo-item/completed])
                 :in $ ?todo-id
                 :where [?e :todo-item/id ?todo-id]]
               db id)))

(defn delete-todo-item [connection {:todo-item/keys [id]}]
  (d/transact connection {:tx-data [[:db/retractEntity [:todo-item/id id]]]}))

(pc/defmutation todo-delete [{:keys [connection]} {:todo-item/keys [id]}]
  {::pc/sym    `app.model.todo/todo-delete
   ::pc/output [:todo-item/result]}
  (delete-todo-item connection {:todo-item/id id})
  {:todo-item/result "todo deleted"})

(defn save-todo-item [connection todo-data]
  (d/transact connection {:tx-data [todo-data]}))

(pc/defmutation todo-toogle-completed [{:keys [connection]} {:todo-item/keys [id completed]}]
  {::pc/sym    `app.model.todo/todo-toogle-completed
   ::pc/output [:todo-item/result]}
  (save-todo-item connection {:todo-item/id        id
                              :todo-item/completed completed})
  {:todo-item/result "todo toogle saved"})

;; TODO: refactor save and toogle to single write to db
(pc/defmutation todo-save [{:keys [connection] :as env} {:todo-item/keys [id title completed]}]
  {::pc/sym    `app.model.todo/todo-save
   ::pc/output [:todo-item/result]}
  (let [params (or (get-in env [:ast :params]) (get-in env [:ast]))]
    (println (get-in env [:ast :params]))
    (println (get-in env [:ast]))
    (save-todo-item connection {:todo-item/id        id
                                :todo-item/title     title
                                :todo-item/completed completed})
    {:todo-item/result "todo saved"}))

(def resolvers [todo-resolver all-todos-resolver todo-save todo-toogle-completed todo-delete])
