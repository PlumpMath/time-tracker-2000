(ns lobos.migrations
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]]
               core schema config helpers)))

(defmigration add-members-table
  (up [] (create
          (tbl :members
               (varchar :name 200 :unique))))
  (down [] (drop (table :members))))

(defmigration add-clients-table
  (up [] (create
          (tbl :clients
               (varchar :name 500 :not-null)
               (varchar :email 500 :not-null)
               (varchar :abn 500)
               (varchar :phone 500)
               (varchar :address 500))))
  (drop [] (drop (table :clients))))

(defmigration add-jobs-table
  (up [] (create
          (tbl :jobs
               (varchar :name 500)
               (varchar :description 500)
               (refer-to :clients))))
  (down [] (drop (table :jobs))))

(defmigration add-hours-table
  (up [] (create
          (table :hours
               (integer :hour)
               (integer :rate)
               (timestamp :date)
               (refer-to :jobs)
               (refer-to :members))))
  (down [] (drop (table :hours))))
