(ns invoice.core
  (:use compojure.core [invoice.db :as db] hiccup.core)
  (:require [compojure.route :as route]))

(defn build-overview-list [items]
  [:ul (map (fn [item] [:li (str item)]) items)])

(defn overview-html [hours jobs members clients]
  [:div
   [:div#hours (build-overview-list hours)]
   [:div#clients (build-overview-list clients)]
   [:div#members (build-overview-list members)]
   [:div#jobs (build-overview-list jobs)]])

(defn overview [a]
  (let [hours (db/get-query db/hours 0 10 [:id])
        jobs (db/get-query db/jobs 0 10 [:id])
        members (db/get-query db/members 0 10 [:id])
        clients (db/get-query db/clients 0 10 :id :name :email)]
    (html (overview-html hours jobs members clients))))

(defroutes app
  (GET "/" [] overview))
