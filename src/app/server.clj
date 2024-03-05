(ns app.server
  (:require
    [app.db.client :as db]
    [app.model.todo :as todo]
    [clojure.core.async :as async]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw :refer [not-found-handler wrap-api]]
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.resource :refer [wrap-resource]]))

(pc/defresolver index-explorer [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
       (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
       (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(def my-resolvers [todo/resolvers [index-explorer]])

;; setup for a given connect system
(def parser
  (p/parallel-parser
    {::p/env     {::p/reader                 [p/map-reader
                                              pc/parallel-reader
                                              pc/open-ident-reader]
                  ::pc/mutation-join-globals [:tempids]}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers})
                  (p/env-wrap-plugin (fn [env]
                                       (assoc env
                                         :db (db/get-db)
                                         :connection (db/connection))))
                  (p/post-process-parser-plugin p/elide-not-found)
                  p/error-handler-plugin]}))

(def middleware (-> not-found-handler
                    (wrap-api {:uri    "/api"
                               :parser (fn [query] (async/<!! (parser {} query)))})
                    (fmw/wrap-transit-params)
                    (fmw/wrap-transit-response)
                    (wrap-resource "public")
                    wrap-content-type
                    wrap-not-modified))

(defn test-parser [query] (async/<!! (parser {} query)))
(comment
  (test-parser [{[:todo-item/id 1]
                 [:todo-item/id
                  :todo-item/title
                  :todo-item/completed]}])
  (test-parser [{:all-todos
                 [:todo-item/id
                  :todo-item/title
                  :todo-item/completed]}])

  (test-parser [`(app.model.todo/todo-save {:todo-item/id        ~(random-uuid)
                                            :todo-item/title     "make food"
                                            :todo-item/completed false})])
  )
