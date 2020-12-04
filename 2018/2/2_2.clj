(use 'clojure.java.io)

(defn common-letters [id1 id2]
  (map first
       (filter #(= (nth % 0) (nth % 1))
               (map vector id1 id2))))

(defn close-enough?
  ([id1 id2  ] (close-enough? id1 id2 1))
  ([id1 id2 n] (<= (- (count id1)
                      (count (common-letters id1 id2)))
                   n)))

(defn couples [ids]
  (if
      (= (count ids ) 0)
    '()
    (let [h (first ids)
          l (rest ids)]
      (concat (map #(list h %) l)
              (couples l)))))

(defn close-commons [ids] 
  (filter #(apply close-enough? %) (couples ids)))

(with-open [r (reader "./input")]
  (->> r
       line-seq
       close-commons
       (map #(apply str %))
       println))
