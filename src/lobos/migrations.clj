(ns lobos.migrations
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]]
               core schema config helpers)))

(defmigration add-members-table
  (up [] (create
          (tbl :members
               (varchar :name 200 :unique)
               (timestamp :created_on (default (now)))
               (varchar :email 100)
               (varchar :abn 100)
               (varchar :address 500)
               (varchar :bank_branch 100)
               (varchar :bank_name 200)
               (varchar :bank_number 50)
               (varchar :bank_bsb 50))))
  (down [] (drop (table :members))))

(defmigration add-clients-table
  (up [] (create
          (tbl :clients
               (varchar :name 100 :not-null)
               (timestamp :created_on (default (now)))
               (varchar :email 100 :not-null)
               (varchar :abn 50)
               (varchar :phone 20)
               (varchar :title 200)
               (varchar :address 500))))
  (drop [] (drop (table :clients))))

(defmigration add-jobs-table
  (up [] (create
          (tbl :jobs
               (varchar :name 100)
               (timestamp :created_on (default (now)))
               (varchar :description 500)
               (refer-to :clients))))
  (down [] (drop (table :jobs))))

(defmigration add-hours-table
  (up [] (create
          (tbl :hours
               (integer :hour)
               (timestamp :created_on (default (now)))
               (double :rate)
               (date :date)
               (varchar :description 500)
               (refer-to :jobs)
               (refer-to :members))))
  (down [] (drop (table :hours))))

(defmigration add-expenses-table
  (up [] (create
          (tbl :expenses
               (double :price)
               (timestamp :created_on (default (now)))
               (integer :quantity)
               (varchar :description 500)
               (date :date)
               (refer-to :jobs))))
  (down [] (drop (table :expenses))))
