(ns app.client-ui
  (:require
    [app.helpers :as h]
    [app.model.todo :as todo]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li button h3 label a input p]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as mut]
    [com.fulcrologic.fulcro.networking.http-remote :as http]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(defn is-enter? [evt] (= 13 (.-keyCode evt)))
(defn is-escape? [evt] (= 27 (.-keyCode evt)))

(defsc TodoInput [this {:todo-item/keys [id title completed]
                        :ui/keys        [edit-title]
                        :as             props}]
  {:query         [:todo-item/id :todo-item/title :todo-item/completed :ui/edit-title]
   :ident         :todo-item/id
   :initial-state {:todo-item/id        (h/new-id)
                   :todo-item/title     ""
                   :todo-item/completed false}}
  (div
    (if completed
      (div :.ui.disabled.input
           (input :.w-full {:type  "text"
                            :value title
                            :style {:text-decoration "line-through"}}))
      (div :.ui.input
           (input :.w-full {:type        "text"
                            :value       edit-title
                            :placeholder "add todo title"
                            :onChange    #(mut/set-string! this :ui/edit-title :event %)
                            :onBlur      (fn [e]
                                           (comp/transact! this
                                                           [(todo/todo-save {:todo-item/id        id
                                                                             :todo-item/title     (evt/target-value e)
                                                                             :todo-item/completed false})]))})))))


(def ui-todo-input (comp/factory TodoInput))

(defsc TodoDisplay [this {:todo-item/keys [id title completed]
                          ;:ui/keys        [edit-title]
                          }]
  {:query         [:todo-item/id :todo-item/title :todo-item/completed #_:ui/edit-title]
   :ident         :todo-item/id
   :initial-state {}}
  (div
    (input {:type     "checkbox" :name id :checked completed
            :onChange #(comp/transact! this
                                       [(todo/todo-save {:todo-item/id        id
                                                         :todo-item/completed (not completed)})])})
    (if completed
      (div :.ui.disabled.input
           (input :.w-full {:type  "text"
                            :value title
                            :style {:text-decoration "line-through"}}))
      (div :.ui.input
           (input :.w-full {:type        "text"
                            :value       title
                            :placeholder "add todo title"
                            ;:onChange    #(mut/set-string! this :ui/edit-title :event %)
                            :onChange      #(comp/transact!
                                            this
                                            [(todo/todo-save {:todo-item/id    id
                                                              :todo-item/title (evt/target-value %)})])})))))

(def ui-todo-display (comp/factory TodoDisplay))

(defsc TodoList [this {:todo-list/keys [todos]}]
  {:query         [{:todo-list/todos (comp/get-query TodoInput)}]
   :ident         (fn [] [:component/id :todo-list])
   :initial-state {:todo-list/todos {}}}
  (div :.ui.segment
       (h3 :.ui.header "Your Todos")
       (ul
         (map (fn [{:todo-item/keys [id] :as todo-item}]
                (div {:key   id
                      :style {:display "flex"}} (ui-todo-display todo-item)
                     (button {:onClick #(comp/transact! this
                                                        [(todo/todo-delete {:todo-item/id id})])}
                             "delete"))) todos))))

(def ui-todo-list (comp/factory TodoList))

(defsc Root [_ {:root/keys [todo-input todo-list] :as props}]
  {:query         [{:root/todo-input (comp/get-query TodoInput)}
                   {:root/todo-list (comp/get-query TodoList)}]
   :initial-state {:root/todo-input {}
                   :root/todo-list  {}}}
  (div :.ui.container.segment
       (div
         (h3 "Add a todo")
         (ui-todo-input todo-input))
       (div
         (ui-todo-list todo-list))))

(defn- init-load-fn [app] (df/load! app :all-todos TodoInput
                                    {:target [:component/id :todo-list :todo-list/todos]}))

(defonce APP (app/fulcro-app {:remotes          {:remote (http/fulcro-http-remote {})}
                              :client-did-mount init-load-fn}))

(defn ^:export init []
  (app/mount! APP Root "app"))

(comment
  ;(tap> APP)
  :com.fulcrologic.fulcro.application/state-atom
  (-> APP
      ::app/state-atom
      deref
      ;(swap!)
      )
  )
