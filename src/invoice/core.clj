(ns invoice.core
  (:use compojure.core [invoice.db :as db]
        ring.middleware.json ring.util.response)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))


(defroutes my-routes
  (GET "/" [] dashboard)

  (GET "/members/new" [] (build-form :members :name))
  (GET "/clients/new" [] (build-form :clients :name :email :abn :phone :address))
  (GET "/jobs/new" [] (build-form :jobs :name :description [:client_id db/clients]))

  (GET "/hours/log" [] log-hours-form)

  (POST "/clients" [] add-clients)
  (POST "/hours" [] add-hours)
  (POST "/jobs" [] add-jobs)
  (POST "/members" [] add-members)

  ;(GET "/pdf" [] build-pdf-form)

  (route/resources "/"))

(def app
  (-> (handler/site my-routes)
      (wrap-base-url)
      (wrap-json-body my-routes)))
