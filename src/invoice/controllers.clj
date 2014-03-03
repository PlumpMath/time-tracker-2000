(ns invoice.controllers
  (:use [invoice.db :as db]
        [invoice.views :as views]
        [clj-time.format :as tf]))

(defn str->date [s]
  "Converts DD/MM/YYYY to parsed date string"
  (str (tf/parse (tf/formatter "dd/MM/yyyy") s)))

(defn str->int [s]
  (let [n (read-string s)]
       (if (number? n) n nil)))

(defn dashboard [req]
  "Renders the dashboard page"
  (views/render
   (map views/overview
        [{:name "hours"
           :results (db/find-all db/hours 10 0 :id :hour :rate :date :description)}
         {:name "clients"
           :results (db/find-all db/clients 10 0 :id :email :name)}
         {:name "members"
          :results (db/find-all db/members 10 0 :id :name)}
         {:name "jobs"
          :results (db/find-all db/jobs 10 0 :id :name :description)}])))

(defn add-client-view [req]
  "Builds the view for adding new clients"
  (views/render
   (list (views/title "Add Clients")
         (views/build-form "n" "post" "/clients"
                           (views/build-form-input "Name" :name)
                           (views/build-form-input "Email" :email)
                           (views/build-form-input "ABN" :abn)
                           (views/build-form-input "Phone" :phone)
                           (views/build-form-input "Address" :address)))))

(defn add-member-view [req]
  "Builds the view for adding new members to the team"
  (views/render
   (list (views/title "Add Member")
         (views/build-form "n" "post" "/members"
                           (views/build-form-input "Name" :name)))))

(defn add-job-view [req]
  "Builds the view for adding new members to the team"
  (let [clients (db/find-all db/clients -1 0 :id :name)]
    (views/render
     (list (views/title "Add Job")
           (views/build-form "n" "post" "/jobs"
                             (views/build-form-input "Name" :name)
                             (views/build-form-input "Description" :description)
                             (views/build-form-select "Client" :client_id clients))))))

(defn add-expenses-view [req]
  "Builds the view for adding expenses to a job"
  (let [jobs (db/find-all db/jobs -1 0 :id :name)]
    (views/render
     (list (views/title "Add Expenses")
           (views/build-form "n" "post" "/expenses"
                             (views/build-form-input "Quantity" :quantity)
                             (views/build-form-input "Price" :price)
                             (views/build-form-input "Description" :description)
                             (views/build-form-select "Job" :job_id jobs))))))

(defn log-hours-view [req]
  "Constructs the view for logging hours"
  (let [jobs (db/find-all db/jobs -1 0 :name :id)
        members (db/find-all db/members -1 0 :name :id)]
    (views/render
     (list (views/title "Add Hours")
           (views/build-form "n" "post" "/hours"
                             (views/build-form-input "Hours" :hour)
                             (views/build-form-input "Rate" :rate)
                             (views/build-form-input "Date (DD/MM/YYYY)" :date)
                             (views/build-form-select "Member" :member_id members)
                             (views/build-form-select "Job" :job_id jobs))))))

(defn add-hours [req]
  "Logs new hours to the database"
  (let [req-body (:params req)]
    (db/add-to-db db/hours req-body)))

(defn add-clients [req]
  "Adds a new client to the database"
  (let [req-body (:params req)]
    (db/add-to-db db/clients req-body)))

(defn add-jobs [req]
  "Adds a new job to the database"
  (let [req-body (:params req)]
    (db/add-to-db db/jobs req-body)))

(defn add-members [req]
  "Adds a new member to the database"
  (let [req-body (:params req)]
    (db/add-to-db db/members req-body)))

(defn add-expenses [req]
  (let [body (:params req)]
    (db/add-to-db db/expenses body)))
