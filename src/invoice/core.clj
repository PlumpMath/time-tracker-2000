(ns invoice.core
  (:use compojure.core [invoice.db :as db]
        hiccup.core hiccup.page hiccup.element
        [hiccup.middleware :only (wrap-base-url)]
        ring.middleware.json ring.util.response)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defn build-overview-row [item]
  [:tr
   (for [[k v] item] [:td (str v)])
   [:td
    [:a {:href (str "/delete")} "X"]]])

(defn build-overview-table [items]
  [:table {:style "width:100%"}
   [:thead
    [:tr
     (for [[k v] (first items)] [:th (name k)])
     [:td "Delete"]]]
   [:tbody
    (map build-overview-row items)]])

(defn overview-html [table]
  [(keyword (str "div#" (:name table)))
   (list
    [:h3 (:name table)]
    (if (empty? (:results table))
      [:p "empty"]
      (build-overview-table (:results table)))
    [:hr])])

(def head
  (list [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1.0"}]
        [:meta {:charset "utf-8"}]
        [:title "Invoice"]
        (include-css "/css/normalize.css" "/css/foundation.min.css")))

(defn skelo [body]
  (html
   [:html {:class "no-js" :lang "en"}
    [:head head]
    [:body (list
            (include-js "/js/jquery.js" "/js/foundation.min.js")
            [:nav {:class "top-bar" :data-topbar true}
             [:ul {:class "title-area"}
              [:li {:class "name"}
               [:h1
                [:a {:href "#"} "Invoice 2000"]]]]
              [:section {:class "top-bar-section"}
               [:ul {:class "right"}
                [:li {:class "active"}
                 [:a {:href "/"} "Dashboard"]]
                [:li {:class "has-dropdown"}
                 [:a {:href "#"} "New"]
                 [:ul {:class "dropdown"}
                  [:li
                   [:a {:href "/members/new"} "Member"]
                   [:a {:href "/clients/new"} "Client"]
                   [:a {:href "/jobs/new"} "Job"]]]]
                [:li
                 [:a {:href "/hours/log"} "Log Hours"]]
                [:li
                 [:a {:href "/buid_pdf"} "PDF"]]]]]
            [:div {:class "row"}
             [:div {:class "large-12 columns"} body]]
            (javascript-tag "$(document).foundation()"))]]))

(defn dashboard [a]
  (skelo
   (map overview-html
        [{:name "hours"
           :results (db/get-query db/hours 10 0 :id :hour :rate :date :description)}
         {:name "clients"
           :results (db/get-query db/clients 10 0 :id :email :name)}
         {:name "members"
          :results (db/get-query db/members 10 0 :id :name)}
         {:name "jobs"
          :results (db/get-query db/jobs 10 0 :id :name :description)}])))

(defn build-form-input [input]
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:label (name input)]
    [:input {:type "text"
             :name (name input)}]]])

(defmacro my-eval [s] `~(read-string (str s)))

(defn build-form-submit [v]
  "v: submit value"
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:input {:class "button"
             :type "submit"
             :value v}]]])

(defn build-form [n & args]
  "n: name of table, args: column names"
  (let [table (my-eval (str "db/" (name n)))]
    (skelo
     (list [:h1 (str "new " (name n))]
           [:form {:method "post"
                   :action (str "/" (name n))}
            (map build-form-input args)
            [:hr]
            (build-form-submit "Add")]))))

(defn build-form-select [n options]
  "n: select name, options: select options"
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:label (name n)]
    [:select {:name (name n)}
     (map (fn [option]
            [:option {:value (:id option)} (:name option)]) options)]]])

(defn log-hours-form [a]
  (let [jobs (db/get-query db/jobs 99 0 :name :id)
        members (db/get-query db/members 99 0 :name :id)]
    (skelo
     (list [:h1 "Log Hours"]
           [:form {:method "post" :action "/hours"}
            (build-form-input :hour)
            (build-form-input :rate)
            (build-form-input :date)
            (build-form-input :description)
            (build-form-select :member_id members)
            (build-form-select :job_id jobs)
            (build-form-submit "Log")]))))

(defn make-body [body & vals]
  (apply array-map (flatten (map (fn [val] [val (val body)]) vals))))

(defn add-hours [req]
  (let [req-body (:params req)]
    (db/add-to-db db/hours req-body)))

(defn add-clients [req]
  (let [req-body (:params req)]
    (db/add-to-db db/clients req-body)))

(defn add-jobs [req]
  (let [req-body (:params req)]
    (db/add-to-db db/jobs req-body)))

(defn add-members [req]
  (let [req-body (:params req)]
    (db/add-to-db db/members req-body)))

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
