(ns invoice.controllers
  (:use [invoice.db :as db]
        [invoice.views :as views]
        [clj-time.format :as tf]))

(def db-format (tf/formatter "yyyy-MM-dd"))
(def my-format (tf/formatter "dd/MM/yyyy"))

(defn str->date [s]
  "Converts DD/MM/YYYY to parsed date string"
  (tf/unparse db-format (tf/parse my-format s)))

(defn date->str [s]
  "Converts db date string to DD/MM/YYYY"
  (tf/unparse my-format (tf/parse db-format s)))

(defn str->int [s]
  (if (nil? s)
    nil
    (let [n (read-string s)]
      (if (number? n) n nil))))

(defn mk-action [route id]
  (if (nil? id)
    route
    (str route "/" id)))

(defn unparse-value [v t]
  "Unparses value based on type"
  (if (nil? v)
    nil
    (cond
     (= t :date) (date->str (str v))
     :else v)))

(defn parse-value [v t]
  (println v)
  (println t)
  "Parses value based on type"
  (cond
   (= t :date) (str->date (str v))
   (= t :integer) (str->int v)
   :else v))

(defn build-crud-form [schema form]
  "Builds a crud form to add/edit data"
  (map #(do (let [k (key %)
                  v (val %)]
              (if (= (:type v) :relationship)
                (let [foreign (db/find-all (:refers v) -1 0 :id :name)]
                  (views/build-form-select (:name v) k foreign (k form)))
                (views/build-form-input
                 (:name v)
                 k
                 (unparse-value (k form) (:type v))))))
       schema))

(defn commit [schema body]
  (into {} (map #(let [k (key %)
                       v (val %)]
                   {k (parse-value (k body) (:type v))}
                  ) schema)))

(defn dashboard [req]
  "Renders the dashboard page"
  (views/render
   (map views/overview
        [{:name "hours"
          :results (db/find-all db/hours 10 0 :id :hour :rate :date :description)}
         {:name "expenses"
          :results (db/find-all db/expenses 10 0 :id :quantity :price :date :description)}
         {:name "clients"
           :results (db/find-all db/clients 10 0 :id :email :name)}
         {:name "members"
          :results (db/find-all db/members 10 0 :id :name)}
         {:name "jobs"
          :results (db/find-all db/jobs 10 0 :id :name :description)}])))

(defn get-clients [req]
  "Builds the view for adding new clients"
  (let [id (:id (:params req))
        form (db/find-one db/clients id)]
    (println form)
    (views/render
     (list (views/title "Add Clients")
           (views/build-form
            "post"
            (mk-action "/clients" id)
            (build-crud-form db/client-schema form))))))

(defn get-members [req]
  "Builds the view for adding new members to the team"
  (let [id (:id (:params req))
        form (db/find-one db/members id)]
    (views/render
     (list (views/title "Add Member")
           (views/build-form
            "post"
            (mk-action "/members" id)
            (build-crud-form db/member-schema form))))))

(defn get-jobs [req]
  "Builds the view for adding new members to the team"
  (let [id (:id (:params req))
        form (db/find-one db/jobs id)]
    (views/render
     (list (views/title "Add Job")
           (views/build-form
            "post"
            (mk-action "/jobs" id)
            (build-crud-form db/job-schema form))))))

(defn get-expenses [req]
  "Builds the view for adding expenses to a job"
  (let [id (:id (:params req))
        form (db/find-one db/expenses id)]
    (views/render
     (list (views/title "Add Expenses")
           (views/build-form
            "post"
            (mk-action "/expenses" id)
            (build-crud-form db/expense-schema form))))))

(defn get-hours [req]
  "Constructs the view for logging hours"
  (let [id (:id (:params req))
        form (db/find-one db/hours id)]
    (views/render
     (list (views/title "Add Hours")
           (views/build-form
            "post"
            (mk-action "/hours" id)
            (build-crud-form db/hours-schema form))))))

(defn add-hours [req]
  "Logs new hours to the database"
  (let [body (:params req)]
    (db/add-to-db db/hours (commit db/hours-schema body))))

(defn add-clients [req]
  "Adds a new client to the database"
  (let [body (:params req)]
    (db/add-to-db db/clients (commit db/client-schema body))))

(defn add-jobs [req]
  "Adds a new job to the database"
  (let [body (:params req)]
    (db/add-to-db db/jobs (commit db/job-schema body))))

(defn add-members [req]
  "Adds a new member to the database"
  (let [body (:params req)
        id (:id body)]
    (db/add-to-db db/members (commit db/member-schema body))))

(defn add-expenses [req]
  (let [body (:params req)]
    (db/add-to-db db/expenses (commit (db/expense-schema body)))))
