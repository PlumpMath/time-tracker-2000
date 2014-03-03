(ns invoice.core
  (:use compojure.core [invoice.controllers :as controllers]
        ring.middleware.json ring.util.response
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defn foo [req & {:as params}]
  (println (:id (:params req)))
  (println params)
  (str "foo"))

(defroutes my-routes
  (GET "/" [] dashboard)

  (GET "/members/new" [] controllers/get-members)
  (GET "/clients/new" [] controllers/get-clients)
  (GET "/jobs/new" [] controllers/get-jobs)
  (GET "/expenses/new" [] controllers/get-expenses)
  (GET "/hours/new" [] controllers/get-hours)

  (POST "/clients" [] controllers/add-clients)
  (POST "/hours" [] controllers/add-hours)
  (POST "/jobs" [] controllers/add-jobs)
  (POST "/members" [] controllers/add-members)
  (POST "/expenses" [] controllers/add-expenses)

  (GET "/members/:id" [id] controllers/get-members)
  (GET "/clients/:id" [id] controllers/get-clients)
  (GET "/jobs/:id" [id] controllers/get-jobs)
  (GET "/expenses/:id" [id] controllers/get-expenses)
  (GET "/hours/:id" [id] controllers/get-hours)

  ;(GET "/pdf" [] build-pdf-form)

  (route/resources "/"))

(def app
  (-> (handler/site my-routes)
      (wrap-base-url)
      (wrap-json-body my-routes)))
