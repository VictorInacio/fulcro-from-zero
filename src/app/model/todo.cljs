(ns app.model.todo
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp]))

(defn picker-path [& ks] (into [:todo-item/id] ks))

(defmutation add-todo [{:todo-item/keys [title] :as params}]
  (action [{:keys [state]}]
          (js/console.log "OK" )
          (js/console.log state )
          (js/console.log title )
          #_(swap! state assoc-in (todo-path id) inc))
  (ok-action [env] (js/console.log "OK"))
  (error-action [env] (js/alert "BAD!"))
  (remote [env] true))

(comment
  (comp/transact! this
                  [(add-todo {:person/id id})]
                  {:refresh [:person-list/people]})

  )