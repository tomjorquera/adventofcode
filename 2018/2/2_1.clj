(use 'clojure.java.io)

(defn letter_counts [line]
  (reduce
   (fn [counts letter]
     (update counts letter (fn [c] (+ (if (nil? c) 0 c) 1))))
   {} line))

(defn has? [line_counts n]
  (contains? (set (vals line_counts)) n))

(defn counts [line_counts]
  {:2 (if (has? line_counts 2) 1 0)
   :3 (if (has? line_counts 3) 1 0)})

(def total (partial
            reduce
            (fn [c1 c2] {:2 (+ (get c1 :2)
                               (get c2 :2))
                         :3 (+ (get c1 :3)
                               (get c2 :3))})))

(with-open [r (reader "./input")]
  (->> r
       line-seq
       (map letter_counts)
       (map counts)
       total
       ((fn [total] (* (get total :2)
                       (get total :3))))
       println))
