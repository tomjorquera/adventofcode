(use 'clojure.java.io)

(def serial 1718)

(defn power-level [x y serial]
  (let [rack-id (+ x 10)
        pow-lvl (* rack-id y)
        pow-lvl (+ pow-lvl serial)
        pow-lvl (* pow-lvl rack-id)
        pow-lvl (read-string (str (nth (str pow-lvl) (- (count (str pow-lvl)) 3))))
        pow-lvl (- pow-lvl 5)]
    pow-lvl))

(defn grid-power-level [x y serial]
  (let [coords (for [x (range x (+ x 3))
                     y (range y (+ y 3))] (list x y))
        powers (map #(power-level (nth % 0) (nth % 1) serial) coords)
        total-power (reduce + powers)]
    total-power))

(defn grid [x-max y-max serial]
  (into {} (for [x (range 0 (- x-max 2))
                 y (range 0 (- y-max 2))]
             [[x y] (grid-power-level x y serial)])))

(defn part1 []
  (apply max-key val (grid 300 300 serial)))

(println (part1))

(defn power-level [x y serial]
  (let [pow-lvl  (+ (* (* (+ x 10) (+ x 10)) y)
                    (* serial (+ x 10)))
        pow-lvl (read-string (str (nth (str pow-lvl) (- (count (str pow-lvl)) 3))))
        pow-lvl (- pow-lvl 5)]
    pow-lvl))

(defn grid-power [x-max y-max serial]
  (into {} (for [x (range x-max)
                 y (range y-max)]
             [[x y] (power-level x y serial)])))

(defn total-power [grid x y len]
  (apply + (for [x (range x (+ x len))
                 y (range y (+ y len))]
             (get grid [x y]))))


(defn best-range [grid x y max-len]
  (apply max-key val (into {}
                           (map (fn [l] [[x y l] (total-power grid x y l)])
                                (range 1 (+ max-len 1))))))

(defn best [x-max y-max serial]
  (let [grid (grid-power x-max y-max serial)
        max-len (min x-max y-max)]

    (apply max-key val 
           (into {} (for [x (range x-max)
                          y (range y-max)]
                      (do
                        (when (= (mod (+ x y) 10) 0)
                          (println (str "doing " x " " y)))

                        (best-range grid x y (- max-len (max x y)))))))))


;; (defn grid-power-level2 [x y len serial]
;;   (let [coords (for [x (range x (+ x len))
;;                      y (range y (+ y len))] (list x y))
;;         powers (map #(power-level (nth % 0) (nth % 1) serial) coords)
;;         total-power (reduce + powers)]
;;     total-power))

;; (defn grid2 [x-max y-max len serial]
;;   (into {} (for [l (range 0 (+ len 1))
;;                  x (range 0 (- x-max (- l 1)))
;;                  y (range 0 (- y-max (- l 1)))]
;;              [[x y l] (grid-power-level2 x y l serial)])))

;; (defn grid2 [x-max y-max len serial]
;;   (into {} (for [x (range 0 (- x-max (- len 1)))
;;                  y (range 0 (- y-max (- len 1)))
;;                  l (range 90 (+ (- len (max x y)) 1))]
;;              [[x y l] (grid-power-level2 x y l serial)])))


;; (defn part2 []
;;   (apply max-key val (grid2 300 300 100 serial)))

(defn part2 []
  (best 300 300 serial))

(println (java.util.Date.))
(println (part2))
(println (java.util.Date.))
