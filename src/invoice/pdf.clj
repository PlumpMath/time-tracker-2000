(ns invoice.pdf
  (:use [clojure.java.shell :only [sh]]))

(defn space-out [l]
  (clojure.string/join " " l))

(defn writelatex [& txt]
  (space-out (map to-latex txt)))

(defn to-latex [v]
  (cond
   (keyword? v) (str "\\" (name v))
   (vector? v) (str "{" (space-out (flatten (map to-latex v))) "}")
   (string? v) v))

(defn % [s] (str "% " s "\n"))

(def newline "\\\\")

(def date (writelatex {:bf "Date:" newline :tab :today newline}))

(defn title [t]
  (writelatex :hifil [:large :bf t] :hifil :bigskip :break :hrule))

(defn hourrow [description hours price]
  (writelatex :hourrow [description] [(str hours)] [(str price)]))

(defn feerow [description price]
  (writelatex :feerow [description] [(str price)]))

(defn begin [n & opts]
  (writelatex :begin [n] (apply writelatex opts) :end [n]))

(defn details [t person]
  (writelatex
   [:bf (str t ":")] newline
   (:name person) newline
   "ABN:" (:abn person) newline
   (:address person) newline
   (:email person) newline))

(defn bank-details [person]
  (writelatex
   [:bf "Details for Direct Deposit"] newline
   :tab [:bf "Bank:"] (:branch person) newline
   :tab [:bf "BSB:"] (:bsb person) newline
   :tab [:bf "Account No:"] (:account person) newline
   :tab [:bf "Account Name:" (:name person) newline]))

(def head (writelatex :documentclass ["invoice"]
                      :def :tab [:hspace* ["3ex"]]))

(defn hour-table [hours]
  (writelatex :feetype "Hours Worked" (map hourrow hours)))

(defn expense-table [expenses]
  (writelatex :feetype "Expenses" (map feerow expenses)))

(defn note [s]
  (writelatex [:bf s newline]))

(defn layout [t from to hours expenses & notes]
  (writelatex
   head
   (begin "document"
          (title t)
          (details "Invoice From" from) newline
          (bank-details from) newline
          (details "Invoice To" to) newline
          date
          (begin "invoiceTable"
                 (hour-table hours)
                 (expense-table expenses))
          (apply writelatex (map note notes)))))
