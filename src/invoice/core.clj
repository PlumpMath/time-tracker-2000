(ns invoice.core
  (:use compojure.core [invoice.controllers :as controllers]
        ring.middleware.json ring.util.response
        [hiccup.middleware :only (wrap-base-url)]
        [invoice.db :as db])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defmacro defcontroller
  "Builds the routes for some crud" [n entity schema & fields]
  `(context ~n []
            (POST "/" [] #(controllers/post-form % ~entity ~schema))
            (POST "/:id" [] #(controllers/post-form % ~entity ~schema))
            (GET "/" [] #(controllers/get-list % ~entity [~@fields]))
            (GET "/new" [] #(controllers/get-form % ~entity ~schema))
            (GET "/:id" [] #(controllers/get-form % ~entity ~schema))))

(defroutes my-routes
  (GET "/" [] dashboard)

  (defcontroller "/jobs" db/jobs db/job-schema
    :id :name :description)
  (defcontroller "/members" db/members db/member-schema
    :id :name)
  (defcontroller "/hours" db/hours db/hours-schema
    :id :hour :rate :date :description)
  (defcontroller "/clients" db/clients db/client-schema
    :id :name :email)
  (defcontroller "/expenses" db/expenses db/expense-schema
    :id :quantity :price :description :date)

  (GET "/pdf" [] pdf-form)
  (POST "/pdf" [] mk-pdf)

  (route/resources "/"))

(def app
  (-> (handler/site my-routes)
      (wrap-base-url)
      (wrap-json-body my-routes)))
