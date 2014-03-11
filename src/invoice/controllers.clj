(ns invoice.controllers
  (:use [invoice.db :as db]
        [invoice.views :as views]
        [invoice.pdf :as pdf :only [layout compile-pdf]]
        [clj-time.format :as tf]
        [ring.util.response :as response]
        (korma db core)))

(defmacro get-entity [n]
  `(eval (symbol (str "db/" ~n))))

(def db-format (tf/formatter "yyyy-MM-dd"))
(def my-format (tf/formatter "dd/MM/yyyy"))

(defn str->date [s]
  "Converts DD/MM/YYYY to parsed date string"
  (tf/unparse db-format (tf/parse my-format s)))

(defn date->str [s]
  "Converts db date string to DD/MM/YYYY"
  (tf/unparse my-format (tf/parse db-format s)))

(defn str->int [s]
  (if (not (nil? s))
    (let [n (read-string s)]
      (if (number? n) n))))

(defn mk-action [route id]
  (if (nil? id)
    route
    (str route "/" id)))

(defn mk-pagination [page per-page]
  "Creates skip based on page/per-page"
  (* (- page 1) per-page))

(defn unparse-val [v t]
  "Unparses value based on type"
  (if (nil? v)
    nil
    (cond
     (= t :date) (date->str (str v))
     :else v)))

(defn parse-val [v t]
  "Parses value based on type"
  (cond
   (= t :date) (str->date (str v))
   (= t :integer) (str->int v)
   :else v))

(defn build-crud-form [schema form]
  "Builds a crud form"
  (map #(do (let [k (key %)
                  v (val %)
                  t (:type v)]
              (if (= t :relationship)
                (let [foreign (db/find-all (:refers v) -1 0 :id :name)]
                  (views/build-form-select (:name v) k foreign (k form)))
                (views/build-form-input (:name v) k (unparse-val (k form) t)))))
       schema))

(defn commit [schema body]
  "Parses the request body"
  (into {} (map #(let [k (key %)
                       v (val %)]
                   {k (parse-val (k body) (:type v))})
                schema)))

(defn dashboard [req]
  "Renders the index page"
  (views/render
   (map #(apply views/overview %)
        [["/hours" (db/find-all db/hours 10 0 :id :hour :rate :date :description)]
         ["/expenses" (db/find-all db/expenses 10 0 :id :description :quantity :price :date )]
         ["/clients" (db/find-all db/clients 10 0 :id :name :email)]
         ["/members" (db/find-all db/members 10 0 :id :name)]
         ["/jobs" (db/find-all db/jobs 10 0 :id :name :description)]])))

(defn get-form [req entity schema]
  "Builds the view for the given entity"
  (let [context (:context req)
        id (:id (:params req))
        form (db/find-one entity id)]
    (views/render
     (list (views/title context)
           (views/build-form
            "post"
            (mk-action context id)
            (build-crud-form schema form))))))

(defn post-form [req entity schema]
  "Updates the given entity"
  (let [body (:params req)
        id (:id body)
        result (db/add-to-db id entity (commit schema body))]
    (response/redirect "/")))

(defn get-list [req entity fields]
  "Lists items for the given entity"
  (let [context (:context req)
        page (or (str->int (:page (:params req))) 1)
        l (or (str->int (:per_page (:params req))) 10)
        s (mk-pagination page l)
        results (db/find-all entity l s fields)]
    (views/render
     (list
      (views/overview context results)
      (views/pagination context page l)))))

(defn pdf-form [req]
  (let [members (db/find-all db/members -1 0 :id :name)
        jobs (db/find-all db/jobs -1 0 :id :name)]
    (views/render
     (views/build-pdf-form jobs members))))

(defn mk-pdf [req]
  (let [body (:params req)
        title (:title body)
        date-from (str->date (:from body))
        date-to (str->date (:to body))
        member (db/find-one db/members (:member_id body))
        job (db/find-one db/jobs (:job_id body))
        client (db/find-one db/clients (:client_id job))
        hours (select db/hours
                      (order :date :ASC)
                      (where (and (<= :date date-to)
                                  (>= :date date-from)
                                  (= :job_id (:id job)))))
        expenses (select db/expenses
                         (order :date :ASC)
                         (where (and (<= :date date-to)
                                     (>= :date date-from)
                                     (= :job_id (:id job)))))
        tex (pdf/layout title member client hours expenses)
        pdf-path (pdf/compile-pdf tex)]
    {:status 200
     :headers {"Content-Type" "application/pdf"}
     :body (clojure.java.io/input-stream pdf-path)}))
