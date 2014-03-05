(ns invoice.pdf
  (:use [clojure.java.shell :only [sh]]))

(defn writelatex [& txt]
  (apply str (map to-latex txt)))

(defn to-latex [v]
  (cond
   (keyword? v) (str "\\" (name v))
   (vector? v) (str "{" (apply str (flatten (map to-latex v))) "}")
   (string? v) (str v)))

(defn % [s] (str "% " s "\n"))

(writelatex :documentclass ["invoice"]
            :def :tab [:hspace ["3ex"]]
            :begin ["document"] (% "foo")
            :hfil [:large :bf "Invoice 133"] :hifil)
