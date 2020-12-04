(use 'clojure.java.io)

(with-open [r (reader "./input")]
  (->> r
       line-seq
       (map #(Integer/parseInt %))
       #(reduce +)
       print))

