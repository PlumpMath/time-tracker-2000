(ns invoice.core
  (:use compojure.core [invoice.controllers :as controllers]
        ring.middleware.json ring.util.response)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes my-routes
  (GET "/" [] dashboard)

  (GET "/members/new" [] (controllers/add-member-view))
  (GET "/clients/new" [] (controllers/add-client-view))
  (GET "/jobs/new" [] (controllers/add-job-view))
  (GET "/hours/log" [] (controllers/log-hours-view))

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
