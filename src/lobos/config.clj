(ns lobos.config
  (:require [invoice.db :as db :only [h2-db]])
  (:use (lobos connectivity)))

(open-global db/h2-db)
