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

(defmutation todo-save [{:todo-item/keys [id title completed]
                         :as             todo-save-props}]
  (action [{:keys [state]}]
          (swap! state update-in [:component/id
                                  :todo-list
                                  :todo-list/todos] conj [:todo-item/id id]))
  (ok-action [{:keys [app] :as env}] (js/console.log "OK")
             ;(tap> env)
             #_(df/load! app :all-todos TodoInput
                         {:target [:component/id :todo-list :todo-list/todos]}))
  (error-action [env] (js/alert "BAD REQUEST!"))
  (remote [env] true))

(defmutation todo-toogle-completed [{:todo-item/keys [id completed]}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:todo-item/id id :todo-item/done] completed))
  (ok-action [env]
             (js/console.log env)
             (js/console.log "OK")
             )
  (error-action [env] (js/alert "BAD REQUEST!"))
  (remote [env] true))

(defmutation todo-delete [{:todo-item/keys [id]}]
  (action [{:keys [state]}]
          (fns/dissoc-in state [:todo-item/id id]))
  (ok-action [env] (js/console.log "OK"))
  (error-action [env] (js/alert "BAD REQUEST!"))
  (remote [env] true))

(comment
  (comp/transact! this
                  [(add-todo {:person/id id})]
                  {:refresh [:person-list/people]})

  )