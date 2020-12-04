(use 'clojure.java.io)

(defn part1 []
  (with-open [r (reader "./input")]
    (->> r
         line-seq
         ((fn [_] "Part 1 not implemented")))))

(defn part2 []
  (with-open [r (reader "./input")]
    (->> r
         line-seq
         ((fn [_] "Part 2 not implemented")))))

(println (part1))

(println (part2))
