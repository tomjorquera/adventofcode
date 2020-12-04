(use 'clojure.java.io)

(defrecord Elf [x y pv dmg])
(defrecord Gob [x y pv dmg])
(defrecord Unit [type x y pv dmg])

(defn new-grid [s]
  (let [parse-line (fn [y s]
                     (loop
                         [[[h & tail] line x units] [s [] 0 []]]

                       (if (= h nil)
                         {:line line
                          :units units}

                         (recur (cond
                                  (= h \.) [tail
                                            (conj line false)
                                            (inc x)
                                            units]

                                  (= h \#) [tail
                                            (conj line true)
                                            (inc x)
                                            units]

                                  (= h \E) [tail
                                            (conj line false)
                                            (inc x)
                                            (conj units (Unit. :elf x y 200 3))]

                                  (= h \G) [tail
                                            (conj line false)
                                            (inc x)
                                            (conj units (Unit. :gob x y 200 3))]

                                  :else (throw (Exception. (str "Unexpected char " h))))))))

        lines (map-indexed parse-line s)]
    (reduce (fn [state line] (->> state
                                  (#(update % :grid (fn [grid] (conj grid (:line line)))))
                                  (#(update % :units (fn [grid] (concat grid (:units line)))))))
            {:grid [] :units []}
            lines)))

(defn units-in-order [units]
  (sort (fn [u1 u2]
          (let [[x1 y1] [(:x u1) (:y u1)]
                [x2 y2] [(:x u2) (:y u2)]]
            (if (not= y1 y2)
              (compare y1 y2)
              (compare x1 x2))))
        units))

(defn units-living [units]
  (filter #(> (:pv %) 0) units))

(defn move [unit grid]
  ;; TODO
  grid)

(defn attack [unit grid]
  ;; TODO
  grid) 

(defn turn [unit grid]
  (let [grid (move unit grid)
        grid (attack unit grid)]
    grid))

(defn round [grid]
  (let [grid (update grid
                     :units (fn [_] (units-in-order (:units grid))))]

    (loop [acting-unit acting-unit
           grid grid]
      (if (= acting-unit nil)
        grid

        )))


  (let [units-todo (:units grid)
        unit-done []])
  )

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

(def init-grid (with-open [r (reader "./input")]
                 (->> r
                      line-seq
                      new-grid)))

(println init-grid)
