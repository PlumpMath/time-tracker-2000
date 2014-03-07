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

(def date (writelatex {:bf "Date:" newline :tab :today newline}))

(defn title [t]
  (writelatex :hfil [:Large :bf t] :hfil :bigskip :break :hrule))

(defn hourrow [description hours price]
  (writelatex :hourrow [description] [(str hours)] [(str price)]))

(defn feerow [description price]
  (writelatex :feerow [description] [(str price)]))

(defn begin [n & opts]
  (writelatex :begin [n] (apply writelatex opts) :end [n]))

(defn details [t person]
  (writelatex
   [:bf (str t ":")] newline
   :tab [:bf "Name:"] (:name person) newline
   :tab [:bf "ABN:"] (:abn person) newline
   :tab [:bf "Address:"] (:address person) newline
   :tab [:bf "Email:"] (:email person) newline
   :tab [:bg "Phone Number:"] (:phone person) newline newline))

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
  (writelatex :feetype "Hours Worked" (map hourrow hours)))

(defn expense-table [expenses]
  (writelatex :feetype "Expenses" (map feerow expenses)))

(defn note [s]
  (writelatex [:bf s newline]))

(defn layout [t from to hours expenses & notes]
  (writelatex
   document-head
   (begin "document"
          (title t)
          (details "Invoice From" from)
          (bank-details from)
          (details "Invoice To" to)
          date
          (begin "invoiceTable"
                 (hour-table hours)
                 (expense-table expenses))
          (apply writelatex (map note notes)))))

(defn compile-pdf [tex]
  (let [dir "resources"
        f "tmp"]
    (spit (str dir "/" f ".tex") tex)
    (let [output (sh "pdflatex" (str f ".tex"))]
      (println output)
      (if (= (:exit output) 0)
        (slurp (str dir "/" f ".pdf"))
        (throw (Throwable. (:err output)))))))
