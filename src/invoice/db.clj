(ns invoice.db
  (:use (korma db core)))

(def h2-db (h2 {:db "resources/invoice.db"}))

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

(defn find-all [entity l s & args]
  "Entity, limit, skip & fields"
  (->
   (select* entity)
   (limit l)
   (offset s)
   (#(apply fields % args))
   (exec)))

(defn add-to-db [entity vals]
  (insert entity (values vals)))
