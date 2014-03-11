(ns invoice.pdf
  (:use [clojure.java.shell :only [sh]]))

(defn space-out [l]
  (clojure.string/join " " l))

(defn to-latex [v]
  (cond
   (keyword? v) (str "\\" (name v))
   (vector? v) (str "{" (space-out (flatten (map to-latex v))) "}")
   (string? v) v))

(defn writelatex [& txt]
  (space-out (map to-latex txt)))

(defn % [s] (str "% " s "\n"))

(def newline "\\\\")

(def date (writelatex [:bf "Date Invoiced:"] newline :tab :today newline))

(defn title [t]
  (writelatex :hfil [:Large :bf t] :hfil :bigskip :break :hrule))

(defn hourrow [row]
  (let [description (str (:date row) ": " (:description row))]
    (writelatex :hourrow
                [description]
                [(str (:hour row))]
                [(str (:rate row))])))

(defn feerow [row]
  (let [description (str (:date row) ": " (:description row))]
    (writelatex :feerow
                [description]
                [(str (:price row))])))

(defn begin [n & opts]
  (writelatex :begin [n] (apply writelatex opts) :end [n]))

(defn details [t person]
  (writelatex
   [:bf (str t ":")] newline
   :tab [:bf "Name:"] (:name person) newline
   (if (not (nil? (:title person)))
     (writelatex :tab [:bf "Company:"] (:title person) newline))
   :tab [:bf "ABN:"] (:abn person) newline
   :tab [:bf "Address:"] (:address person) newline
   :tab [:bf "Email:"] (:email person) newline))

(defn bank-details [person]
  (writelatex
   [:bf "Details for Direct Deposit"] newline
   :tab [:bf "Bank:"] (:bank_branch person) newline
   :tab [:bf "BSB:"] (:bank_bsb person) newline
   :tab [:bf "Account No:"] (:bank_number person) newline
   :tab [:bf "Account Name:"] (:bank_name person) newline newline))

(def document-head (writelatex
                    :nonstopmode
                    :documentclass ["invoice"]
                    :def :tab [:hspace* ["3ex"]]))

(defn hour-table [hours]
  (writelatex :feetype ["Hours Worked"] (apply writelatex (map hourrow hours))))

(defn expense-table [expenses]
  (writelatex :feetype ["Expenses"] (apply writelatex (map feerow expenses))))

(defn note [s]
  (writelatex [:bf s newline]))

(defn layout [t from to hours expenses & notes]
  "Generates the latex code for the invoice"
  (writelatex
   document-head
   (begin "document"
          (title t)
          (details "Invoice From" from)
          (bank-details from)
          (details "Invoice To" to)
          (str date)
          (begin "invoiceTable"
                 (hour-table hours)
                 (expense-table expenses))
          (apply writelatex (map note notes)))))

(defn compile-pdf [tex]
  "Runs pdflatex against a tex file. Returns path to pdf"
  (let [dir "resources"
        f "tmp"]
    (spit (str dir "/" f ".tex") tex)
    (let [output (sh "pdflatex" (str f ".tex") :dir dir)]
      (if (= (:exit output) 0)
        (str dir "/" f ".pdf")
        (throw (Throwable. (:err output)))))))
