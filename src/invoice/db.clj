(ns invoice.db
  (:use (korma db core)))

(def h2-db (h2 {:db "resources/invoice.db"}))

(defn schema->entity [schema]
  "Transforms schema into something entity-fields friendly"
  (into [:id] (map #(key %) schema)))

(defentity members
  (table :members)
  (database h2-db))

(defentity clients
  (table :clients)
  (database h2-db))

(defentity jobs
  (table :jobs)
  (database h2-db)
  (has-one clients {:fk :id}))

(defentity hours
  (table :hours)
  (database h2-db)
  (has-one jobs {:fk :id})
  (has-one members {:fk :id}))

(defentity expenses
  (table :expenses)
  (database h2-db)
  (has-one jobs {:fk :id}))

;; Schemas for each entity. Used to build crud forms etc
;;   type: the type (string, integer, date or relationship)
;;   refers: the entity the relationship refers to
;;   name: the name of the field
(def member-schema
  {:name {:type :string :name "Name"}
   :email {:type :string :name "Email"}
   :address {:type :string :name "Address"}
   :abn {:type :string :name "ABN"}
   :bank_branch {:type :string :name "Bank"}
   :bank_bsb {:type :string :name "Account BSB"}
   :bank_number {:type :string :name "Account Number"}})

(def client-schema
  {:name {:type :string :name "Name"}
   :email {:type :string :name "Email"}
   :abn {:type :string :name "ABN"}
   :title {:type :string :name "Title"}
   :address {:type :string :name "Address"}})

(def job-schema
  {:name {:type :string :name "Name"}
   :description {:type :string :name "Description"}
   :client_id {:type :relationship :refers clients :name "Client"}})

(def hours-schema
  {:hour {:type :integer :name "Hours"}
   :rate {:type :integer :name "Rate"}
   :date {:type :date :name "Date"}
   :description {:type :string :name "Description"}
   :job_id {:type :relationship :refers jobs :name "Job"}
   :member_id {:type :relationship :refers members :name "Member"}})

(def expense-schema
  {:price {:type :integer :name "Price"}
   :quantity {:type :integer :name "Quantity"}
   :description {:type :string :name "Description"}
   :date {:type :date :name "Date"}
   :job_id {:type :relationship :refers jobs :name "Job"}})

(defn find-all [entity l s & args]
  "Entity, limit, skip & fields"
  (->
   (select* entity)
   (limit l)
   (offset s)
   (order :created_on :DESC)
   (#(apply fields % (flatten args)))
   (exec)))

(defn find-one [entity id & opts]
  (first (->
          (select* entity)
          (where {:id id})
          opts)))

(defn add-to-db [id entity vals]
  "Upsert. If id is nil will create a new entry otherwise will update"
  (if (nil? id)
    (insert entity (values vals))
    (update entity (set-fields vals) (where {:id id}))))
