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

(defn get-query [entity s l & f]
  "Entity, skip, limit & fields"
  (select entity
          (fields f)
          (limit l)
          (offset s)))
