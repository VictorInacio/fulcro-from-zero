(ns app.client
  (:require
    [app.model.todo :refer [picker-path] :as todo]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li button h3 label a input]]
    [com.fulcrologic.fulcro.networking.http-remote :as http]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.data-fetch :as df]))


(defsc TodoDetail [this {:todo-item/keys [id title completed]}]
  {:query [:todo-item/id :todo-item/title :todo-item/completed]
   :ident :todo-item/id}
  (div :.ui.segment
       (h3 :.ui.header "Item")
       (when id
         (div :.ui.form
              (div :.field
                   (label completed))
              (div :.field
                   (label "Title: ") title)
              #_(button :.ui.button {:onClick (fn []
                                                (comp/transact! this
                                                                [(make-older {:todo-item/id id})]
                                                                {:refresh [:todo-list/todos]}))}
                        "Save")))))

(def ui-todo-detail (comp/factory TodoDetail {:keyfn :todo-item/id}))

(defsc TodoListItem [this {:todo-item/keys [id title completed]}]
  {:query [:todo-item/id :todo-item/title :todo-item/completed]
   :ident :todo-item/id}
  (li :.item
      (input {:type     "checkbox"
              :checked  completed
              #_#_
                      :onChange #(prim/transact! this
                                                 `[(your-mutation {:completed (-> % .-target .-checked)})])})
      (a {:href    "#"
          :onClick (fn []
                     (df/load! this [:todo-item/id id] TodoDetail
                               {:target (picker-path :todo-picker/selected-todo)}))}
         title)))

(def ui-todo-list-item (comp/factory TodoListItem {:keyfn :todo-item/id}))

(defsc TodoList [_ {:todo-list/keys [todos]}]
  {:query         [{:todo-list/todos (comp/get-query TodoListItem)}]
   :ident         (fn [] [:component/id :todo-list])
   :initial-state {:todo-list/todos []}}
  (div :.ui.segment
       (h3 :.ui.header "Todos")
       (ul
         (map ui-todo-list-item todos))))

(def ui-todo-list (comp/factory TodoList))

(defsc TodoPicker [this {:todo-picker/keys [list selected-todo]}]
  {:query         [{:todo-picker/list (comp/get-query TodoList)}
                   {:todo-picker/selected-todo (comp/get-query TodoDetail)}]
   :initial-state {:todo-picker/list {}}
   :ident         (fn [] [:component/id :todo-picker])}
  (div :.ui.two.column.container.grid
       (button {:onClick (fn [evt]
                           (js/console.log this)
                           (js/console.log evt)
                           (comp/transact! this
                                           [(todo/add-todo {:todo-item/title "title"})]))} "add-todo")
       (div :.column
            (ui-todo-list list))
       (div :.column
            (ui-todo-detail selected-todo))))

(def ui-todo-picker (comp/factory TodoPicker {:keyfn :todo-picker/todos}))

(defsc Root [_ {:root/keys [todo-picker]}]
  {:query         [{:root/todo-picker (comp/get-query TodoPicker)}]
   :initial-state {:root/todo-picker {}}}
  (div :.ui.container.segment
       (ui-todo-picker todo-picker)))

(defonce APP (app/fulcro-app {:remotes          {:remote (http/fulcro-http-remote {})}
                              :client-did-mount (fn [app]
                                                  (df/load! app :all-todos TodoListItem
                                                            {:target [:component/id :todo-list :todo-list/todos]}))}))

(defn ^:export init []
  (app/mount! APP Root "app"))

(comment
  (df/load! APP [:todo-item/id 1] TodoDetail))
