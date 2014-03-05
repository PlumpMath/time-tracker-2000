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

(defn hourrow [description hours price]
  (writelatex :hourrow [(str description)] [(str hours)] [(str price)]))

(defn feerow [description price]
  (writelatex :feerow [(str description)] [(str price)]))

(defn layout [title]
  (writelatex
   :documentclass ["invoice"]
   :def :tab [:hspace* ["3ex"]]
   :begin ["document"] (% "foo")
   :hfil [:large :bf title] :hifil))
