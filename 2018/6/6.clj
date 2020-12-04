(use 'clojure.java.io)

(def coordinates-pattern #"(\d+), (\d+)")

(defn coordinates [s]
  (let [[_ x y] (re-matches coordinates-pattern s)]
    {:x (read-string x)
     :y (read-string y)}))

(defn bounding-box [coordinates]
  (loop [coords coordinates
         xmin 99999
         xmax -1
         ymin 99999
         ymax -1]

    (if (= coords ())
      {:xmin xmin :xmax xmax
       :ymin ymin :ymax ymax}

      (let [coord (first coords)
            coords (rest coords)]
        (recur coords
               (min xmin (get coord :x))
               (max xmax (get coord :x))
               (min ymin (get coord :y))
               (max ymax (get coord :y)))))))

(defn distance [p1 p2]
  (+ (Math/abs (- (get p1 :x)
                  (get p2 :x)))
     (Math/abs (- (get p1 :y)
                  (get p2 :y)))))

(defn closest [point candidates]
  (loop [best [(first candidates)]
         candidates (rest candidates)]
    (if (= candidates ())
      best
      (let [candidate (first candidates)
            candidates (rest candidates)]
        (recur (if (= (distance point candidate)
                      (distance point (first best)))
                 (conj best candidate)
                 (if (< (distance point candidate)
                        (distance point (first best)))
                   [candidate]
                   best))
               candidates)))))

(defn on-border? [bbox dot]
  (or (= (get bbox :xmin) (get dot :x))
      (= (get bbox :xmax) (get dot :x))
      (= (get bbox :ymin) (get dot :y))
      (= (get bbox :ymax) (get dot :y))))

(defn part1 []
  (with-open [r (reader "./input")]
    (let [coords (->> r
                      line-seq
                      (map coordinates))
          bbox (bounding-box coords)
          candidates (filter #(not (on-border? bbox %)) coords)
          dots (for [x (range (get bbox :xmin)
                              (+ 1 (get bbox :xmax)))
                     y (range (get bbox :ymin)
                              (+ 1 (get bbox :ymax)))]
                 {:x x
                  :y y})]

      (->> dots
           (map #(closest % candidates))
           (filter #(= 1 (count %)))
           (map first)
           frequencies
           (apply max-key val)))))

(defn total-distance [point coords]
  (apply + (map #(distance point %) coords)))

(defn part2 []
  (with-open [r (reader "./input")]
    (let [coords (->> r
                      line-seq
                      (map coordinates))
          bbox (bounding-box coords)
          dots (for [x (range (get bbox :xmin)
                              (+ 1 (get bbox :xmax)))
                     y (range (get bbox :ymin)
                              (+ 1 (get bbox :ymax)))]
                 {:x x
                  :y y})]

      (->> dots
           (map #(total-distance % coords))
           (filter #(< % 10000))
           count))))

(println (part1))

(println (part2))
