(ns app.model.todo
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp]))

(defn picker-path [& ks] (into [:todo-item/id] ks))

(defmutation add-todo [{:todo-item/keys [title] :as params}]
  (action [{:keys [state]}]
          (js/console.log "OK")
          (js/console.log state)
          (js/console.log title)
          #_(swap! state assoc-in (todo-path id) inc))
  (ok-action [env] (js/console.log "OK"))
  (error-action [env] (js/alert "BAD REQUEST!"))
  (remote [env] true))

(defn sync-todo-list [state id]
  (swap! state update-in [:component/id
                          :todo-list
                          :todo-list/todos] #(->  %
                                                  set
                                                  (conj [:todo-item/id id])
                                                  vec)))

(defn sync-todo-item-id [state {:todo-item/keys [id title completed] :as todo-save-props}]
  (swap! state update-in [:todo-item/id id] merge todo-save-props))

(defmutation todo-save [{:todo-item/keys [id title completed]
                         :as             todo-save-props}]
  (action [{:keys [state]}]
          (sync-todo-list state id)
          (sync-todo-item-id state todo-save-props))
  (ok-action [{:keys [app] :as env}] (js/console.log "OK")
             ;(tap> env)
             #_(df/load! app :all-todos TodoInput
                         {:target [:component/id :todo-list :todo-list/todos]}))
  (error-action [env] (js/alert "BAD REQUEST!"))
  (remote [env] true))


(defn unsync-todo-item [state id]
  (swap! state update-in [:component/id
                          :todo-list
                          :todo-list/todos] (constantly [])))

(comment
  (disj #{[:todo-item/id 1]} [:todo-item/id 1]))

(defmutation todo-delete [{:todo-item/keys [id]}]
  (action [{:keys [state]}]
          (let [new-state (fns/dissoc-in @state [:todo-item/id id])]
            ;(js/alert (:todo-item/id @state))
            ;(js/alert (:todo-item/id new-state))
            (swap! state fns/dissoc-in [:todo-item/id id])
            (unsync-todo-item state [:todo-item/id id])))
  (ok-action [env] (js/console.log "OK"))
  (error-action [env] (js/alert "BAD REQUEST!"))
  (remote [env] true))

(comment
  (comp/transact! this
                  [(add-todo {:person/id id})]
                  {:refresh [:person-list/people]})

  )