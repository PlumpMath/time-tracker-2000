(ns invoice.controllers
  (:use [invoice.db :as db]
        [invoice.views :as views]))

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
   (list (views/title "Add clients")
         (views/build-form "n" "post" "/clients"
                           (build-form-input :name)
                           (build-form-input :email)
                           (build-form-input :abn)
                           (build-form-input :phone)
                           (build-form-input :address)))))

(defn add-member-view [req]
  "Builds the view for adding new members to the team"
  (views/render
   (list (views/title "Add Member")
         (views/build-form "n" "post" "/members"
                           (build-form-input :name)))))

(defn add-job-view [req]
  "Builds the view for adding new members to the team"
  (let [clients (db/get-query db/jobs -1 0 :name :id)]
    (views/render
     (list (views/title "Add Job")
           (views/build-form "n" "post" "/jobs"
                             (build-form-input :name)
                             (build-form-input :description)
                             (build-form-form :client_id clients))))))

(defn log-hours-view [req]
  "Constructs the view for logging hours"
  (let [jobs (db/find-all db/jobs -1 0 :name :id)
        members (db/find-all db/members -1 0 :name :id)]
    (views/render
     (list (views/title "Add hours")
           (views/build-form "n" "post" "/hours"
                             (build-form-input :hour)
                             (build-form-input :rate)
                             (build-form-input :date)
                             (build-form-input :date)
                             (build-form-select :member_id members)
                             (build-form-select :job_id jobs)
                             (build-form-select "Log"))))))

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
