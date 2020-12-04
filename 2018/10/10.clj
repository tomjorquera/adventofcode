(use 'clojure.java.io)

(def dot-pattern #"position=< ?(-?\d+),  ?(-?\d+)> velocity=< ?(-?\d+),  ?(-?\d+)>")

(defrecord Dot [x y
                vx vy])

(defn dot [s]
  (let [[_ x y vx vy] (re-matches dot-pattern s)]
    (Dot. (read-string x) (read-string y)
          (read-string vx) (read-string vy))))

(defn move [dot]
  (->> dot
       (#(update % :x (fn [x] (+ x (:vx %)))))
       (#(update % :y (fn [y] (+ y (:vy %)))))))

(defn step [dots]
  (map move dots))

(defn step-n [dots n]
  (nth (iterate step dots) n))

(defn step-until [dots pred]
  (loop [dots dots]
    (if (pred dots)
      dots
      (recur (step dots)))))


(defn step-until-and-count [dots pred]
  (loop [dots dots
         nbsteps 0]
    (if (pred dots)
      [dots nbsteps]
      (recur (step dots) (inc nbsteps)))))

(defn display [dots]
  (let [x-min (apply min (map #(:x %) dots))
        x-max (inc (apply max (map #(:x %) dots)))
        y-min (apply min (map #(:y %) dots))
        y-max (inc (apply max (map #(:y %) dots))) 
        grid (reduce
              (fn [grid dot]
                (assoc grid (list (:x dot) (:y dot)) 1))
              {}
              dots)]
    (apply str
           (for [y (range y-min y-max)]
             (apply str (conj
                         (doall (for [x (range x-min x-max)]
                                  (if (get grid (list x y)) "#" ".")))
                         "\n"))))))

(defn part1 []
  (with-open [r (reader "./input")]
    (let [dots (->> r
                    line-seq
                    (map dot)
                    )]
      (->> dots
           doall)))) 

;; Yep... I guessed :D
(defn y-lower-than-16 [dots]
  (let [ys (map #(:y %) dots)
        ymin (apply min ys)
        ymax (apply max ys)]
    (< (- ymax ymin) 16)))


;;(println (display (step-until (part1) y-lower-than-16)))

(let [[dots steps] (step-until-and-count (part1) y-lower-than-16)]
  (do
    (println (display dots))
    (println steps)))


;; (defrecord Grid [dots xmin xmax ymin ymax])

;; (defn grid [dots]
;;   (loop [grid (Grid. {} 999999 -999999 999999 -999999)
;;          dots dots]
;;     (if (= dots ())
;;       grid
;;       (let [dot (first dots)
;;             dots (rest dots)
;;             grid (->> grid
;;                       (#(update % :xmin (fn [xmin] (min xmin (:x dot)))))
;;                       (#(update % :xmax (fn [xmax] (max xmax (:x dot)))))
;;                       (#(update % :ymin (fn [ymin] (min ymin (:y dot)))))
;;                       (#(update % :ymax (fn [ymax] (max ymax (:y dot)))))
;;                       (#(update % :dots (fn [dots] (conj dots [[(:x dot) (:y dot)] 1])))))]
;;         (recur grid dots)))))

;; (defn display-grid [grid]
;;   (apply str
;;          (for [y (range (:ymin grid) (:ymax grid))]
;;            (apply str (conj
;;                        (doall
;;                         (for [x (range (:xmin grid) (:xmax grid))]
;;                           (if (get (:dots grid) (list x y)) "#" ".")))
;;                        "\n")))))
