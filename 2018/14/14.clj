(def input 909441)

(defrecord Board [recipes currents])

(defn new-board []
  (Board. [3 7] [0 1]))

(defn new-recipes [r1 r2]
  (map #(read-string (str %)) (str (+ r1 r2))))

(defn step [board]
  (let [currents (map #(nth (:recipes board) %)
                      (:currents board))
        [r1 r2] currents
        new-rs (new-recipes r1 r2)
        recipes (into (:recipes board) new-rs)
        nb-recipes (count recipes)
        new-pos (map-indexed #(mod (+ %2
                                      (nth currents %1) 1)
                                   nb-recipes)
                             (:currents board))]
    (Board. recipes new-pos)))

(defn take-after [nb-recipes nb-takes]
  (let [board (new-board)
        nb-required (+ nb-recipes nb-takes)
        board (loop [board board]
                (if (< (count (:recipes board))
                       nb-required)
                  (recur (step board))
                  board))]
    (take 10 (drop nb-recipes(:recipes board)))))

(defn part1 []
  (apply str (take-after input 10)))

(defn nb-until [recipes-seq]
  (let [board (new-board)
        seq-size (count recipes-seq)
        board (loop [board board]
                (if (not= (take-last seq-size (:recipes board))
                          recipes-seq)
                  (recur (step board))
                  board))]
    (- (count (:recipes board)) seq-size)))

(defn part2 []
  (nb-until (map #(read-string (str %)) (str input))))

;; (println (part1))
;;
;; (println (part2))
