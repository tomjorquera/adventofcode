(require '[clojure.string :as string])

(defn string->step [input]
  (let [[dir dist] (split-at 1 input)]
    (list (symbol (apply str dir))
          (Integer/parseInt (string/join dist)))))

(defn string->path [input]
  (map string->step
       (string/split input #",")))

(defn displace [x y dir dist]
  (let [[x-start y-start x-end y-end] (case dir
                                        U [x (- y 1) x (- y dist)]
                                        D [x (+ y 1) x (+ y dist)]
                                        L [(- x 1) y (- x dist) y]
                                        R [(+ x 1) y (+ x dist) y])
        x-step (if (< x-start x-end) 1 -1)
        y-step (if (< y-start y-end) 1 -1)]
    [x-end y-end
     (for [x-i (range x-start (+ x-end x-step) x-step)
           y-i (range y-start (+ y-end y-step) y-step)]
       (list x-i y-i))]))

(defn path->points [path]
 (loop [path path
         points []
         x 0
         y 0]
    (if (empty? path)
      points
      (let [[dir dist] (first path)
            path (rest path)
            [x y points-new] (displace x y dir dist)
            points (concat points points-new)]
        (recur path points x y)))))

(defn manhattan-dist [x1 y1 & {:keys [x2 y2] :or {x2 0 y2 0}}]
  (+ (Math/abs (- x1 x2))
     (Math/abs (- y1 y2))))

(defn common-points [paths]
  (apply clojure.set/intersection (map set paths)))

(defn closest [paths]
  (let [commons (common-points (map path->points paths))]
    (apply (partial min-key #(apply manhattan-dist %))
           commons)))

(->> (slurp "input")
     (#(string/split % #"\n"))
     (map string->path)
     (closest)
     (apply manhattan-dist))

;;;; PART 2

(defn nb-steps [points x y]
  (loop [step 1
         points points]
    (if (empty? points)
      -1
      (let [point (first points)
            points (rest points)]
        (if (= (list x y) point)
          step
          (recur (+ step 1) points))))))

(defn common-timing [paths]
  (let [common (common-points paths)]
    (map (fn [point] (apply + (map #(nb-steps % (first point) (second point))
                                               paths)))
         common)))

(->> (slurp "input")
     (#(string/split % #"\n"))
     (map string->path)
     (map path->points)
     (#(apply min (common-timing %))))

;;;; FOR REPL

(string->step "R101")

(string->path  "R101,L202")

(displace 1 3 'R 3)

(displace 1 3 'U 3)

(path->points (string->path "R2,U3,L1"))

(nb-steps
 (path->points (string->path "R2,U3,L1"))
 2 -1)

(->> "R75,D30,R83,U83,L12,D49,R71,U7,L72\nU62,R66,U55,R34,D71,R55,D58,R83"
     (#(string/split % #"\n"))
     (map string->path)
     (closest)
     (apply manhattan-dist))

(->> "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51\nU98,R91,D20,R16,D67,R40,U7,R15,U6,R7"
     (#(string/split % #"\n"))
     (map string->path)
     (closest)
     (apply manhattan-dist))

(->> "R75,D30,R83,U83,L12,D49,R71,U7,L72\nU62,R66,U55,R34,D71,R55,D58,R83"
     (#(string/split % #"\n"))
     (map string->path)
     (map path->points)
     (#(apply min (common-timing %))))

(->> "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51\nU98,R91,D20,R16,D67,R40,U7,R15,U6,R7"
     (#(string/split % #"\n"))
     (map string->path)
     (map path->points)
     (#(apply min (common-timing %))))
