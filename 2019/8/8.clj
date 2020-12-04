(require '[clojure.string :as string])

(defn str->layer [input x]
  (loop [in input
          out []]
    (if (empty? in)
      out
      (let [[row in] (split-at x in)
            row (map #(Integer/parseInt (str %)) row)]
        (recur in (conj out row))))))

(defn str->img [input x y]
  (let [input (string/trim input)
        layer-size (* x y)]
    (loop [input input
           output []]
      (if (empty? input)
        output
        (let [[layer input] (split-at layer-size input)
              layer (str->layer layer x)]
              (recur input (conj output layer)))))))

(defn solution [img]
  (->> img
       (apply min-key (fn [layer] (reduce (fn [acc row] (+ acc
                                                           (count (filter #(= % 0) row))))
                                          0 layer)))
       ((fn [layer]
          (let [nb-ones (reduce (fn [acc row] (+ acc
                                                 (count (filter #(= % 1) row))))
                                0 layer)
                nb-twos (reduce (fn [acc row] (+ acc
                                                 (count (filter #(= % 2) row))))
                                0 layer)]
            (* nb-ones
               nb-twos))))))

(defn collapse [pixels]
  (loop [pixels pixels]
    (if (empty? pixels)
      -1
      (let [[pixel & pixels] pixels]
        (case pixel
          0 0
          1 1
          2 (recur pixels))))))

(defn stacked-img [img]
  (reduce (fn [acc layer]
            (if (nil? acc)
              (map (fn [row] (map vector row))
                   layer)
              (map (fn [row-acc row-layer]
                     (map (fn [item-acc item-layer] (conj item-acc item-layer))
                          row-acc row-layer))
                   acc layer)))
          nil
          img))

(defn render [collapsed-img]
  (string/join 
   (map (fn [row] (str (string/join (map (fn [px]
                                           (case px
                                             0 " "
                                             1 "."))
                                         row))
                       "\n"))
        collapsed-img)))


(->> "input"
     slurp
     (#(str->img % 25 6))
     solution)

(->> "input"
     slurp
     (#(str->img % 25 6))
     stacked-img
     (map (fn [row]
            (map (fn [col] (collapse col)) row)))

     render
     println)
