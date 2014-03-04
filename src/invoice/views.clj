(ns invoice.views
  (:use hiccup.core hiccup.page hiccup.element
        [hiccup.middleware :only (wrap-base-url)]))

(defn title [n]
  "Creates a title"
  [:h1 n])

(defn pagination [n page per-page]
  (let [prev-page (if (= 1 page) nil (- page 1))
        next-page (+ page 1)]
    [:ul {:class "pagination"}
     (if (not (nil? prev-page))
       [:li
        [:a {:href (str n "?page=" prev-page "&per_page=" per-page)
             :class "arrow"} "&laquo;"]]
       [:li
        [:a {:class "unavailable arrow"} "&laquo;"]])
     [:li
      [:a {:href (str n "?page=" next-page "&per_page=" per-page)
           :class "arrow"} "&raquo;"]]]))

(defn overview-table-row [item n]
  [:tr
   (for [[k v] item]
     (if (= k :id)
       [:td [:a {:href (str n "/" v)} v]]
       [:td (str v)]))])

(defn overview-table [items n]
  "Builds the table for the dashboard"
  [:table {:style "width:100%"}
   [:thead
    [:tr
     (for [[k v] (first items)] [:th (name k)])]]
   [:tbody
    (map #(overview-table-row % n) items)]])

(defn overview [context table]
  (list [:h3
         [:a {:href (str context)} context]]
        (if (empty? table)
          [:p "empty"]
          (overview-table table context))))

(def head
  (list [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1.0"}]
        [:meta {:charset "utf-8"}]
        [:title "Invoice"]
        (include-css "/css/normalize.css" "/css/foundation.min.css")
        (include-js "/js/jquery.js" "/js/foundation.min.js")))

(def nav
  [:nav {:class "top-bar" :data-topbar true}
   [:ul {:class "title-area"}
    [:li {:class "name"}
     [:h1
      [:a {:href "/"} "Time Tracker 2000"]]]]
   [:section {:class "top-bar-section"}
    [:ul {:class "right"}
     [:li {:class "active"}
      [:a {:href "/"} "Dashboard"]]
     [:li {:class "has-dropdown"}
      [:a {:href "#"} "Add"]
      [:ul {:class "dropdown"}
       [:li [:a {:href "/members/new"} "Member"]]
       [:li [:a {:href "/clients/new"} "Client"]]
       [:li [:a {:href "/jobs/new"} "Job"]]
       [:li [:a {:href "/expenses/new"} "Expnese"]]
       [:li [:a {:href "/hours/new"} "Hours"]]]]
     [:li
      [:a {:href "/pdf"} "PDF"]]]]])

(defn render [body]
  "Renders the main view"
  (html
   [:html {:class "no-js" :lang "en"}
    [:head head]
    [:body (list
            nav
            [:div {:class "row"}
             [:div {:class "large-12 columns"} body]]
            (javascript-tag "$(document).foundation()"))]]))

(defn build-form-input [n k & v]
  "Builds a input box.  name, key and value"
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:label n]
    [:input {:type "text"
             :name (name k)
             :value (str (first v))}]]])

(defn build-form-submit [v]
  "Builds a submit button. v: submit value"
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:input {:class "button"
             :type "submit"
             :value v}]]])

(defn build-form-select [n k options & v]
  "Builds a select box. n: select name, options: select options"
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:label n]
    [:select {:name (name k)}
     (map #(if (= (:id %) (first v))
             [:option {:value (:id %) :selected "selected"} (:name %)]
             [:option {:value (:id %)} (:name %)])
          options)]]])

(defn build-form-textarea [n k & v]
  [:div {:class "row"}
   [:div {:class "large-4 columns"}
    [:label n]
    [:textarea {:name (name k)
             :value (str (first v))}]]])

(defn build-form [method action & inputs]
  "Builds a form."
  [:form {:method method :action action}
   (list
    inputs
    (build-form-submit "Submit"))])

(defn build-pdf-form [jobs members]
  "Builds the form for generating invoices"
  (list
   [:h2 "Generate invoice"]
   [:hr]
   [:form {:method "post" :action "/pdf"}
    [:h3 "Invoice details"]
    (build-form-input "Title" "title")
    (build-form-textarea "Notes" "notes")
    [:h3 "Invoice between"]
    (build-form-input "From (DD/MM/YYYY)" "from")
    (build-form-input "To (DD/MM/YYYY)" "to")
    [:h3 "Invoice To"]
    (build-form-select "Job" "job_id" jobs)
    [:h3 "Invoice From"]
    (build-form-select "Member" "member_id" members)
    (build-form-submit "Generate")]))
