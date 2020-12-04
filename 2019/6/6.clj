(require '[clojure.string :as string])

(defn str->orbits [input]
             (->> input
                  (#(string/split % #"\n"))
                  (map #(string/split % #"\)"))
                  (map (fn [l] (let [[a b] l] [(keyword b) (keyword a)])))
                  (into {})))

(defn count-orbits-of [orbits object]
  (let [orbited (get orbits object)]
    (case orbited
      nil 0
      (+ 1 (count-orbits-of orbits orbited)))))

(defn count-orbits [orbits]
  (reduce (fn [acc i] (+ acc (count-orbits-of orbits i))) 0 (keys orbits)))

(->> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L"
     (str->orbits)
     (count-orbits))

(->> "input"
     (slurp)
     (str->orbits)
     (count-orbits))

;;;; PART 2

(defn orbits-of [orbits object]
  (let [orbited (get orbits object)]
    (case orbited
      nil '()
      (conj (orbits-of orbits orbited) orbited ))))

(defn orbit-lenght [orbits start end]
  (let [orbited (get orbits start)]
    (cond 
      (= start end) 0
      (nil? orbited) nil
      (= orbited end) 0
      :else (let [sublength (orbit-lenght orbits orbited end)]
              (if sublength
                (+ sublength 1)
                nil)))))

(->> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L"
     (str->orbits))

(->> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L"
     (str->orbits)
    (#(orbits-of % :C)))

(-> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L"
     (str->orbits)
     (orbit-lenght :B :COM))

(-> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L"
    (str->orbits)
    (orbit-lenght :E :B))

(defn nb-transferts [orbits start end]
  (let [orbited-start (orbits-of orbits start)
        orbited-end (orbits-of orbits end)
        root (loop [candidates orbited-start]
               (if (nil? candidates)
                 nil
                 (let [h (first candidates)
                       t (rest candidates)]
                   (if (some #{h} orbited-end)
                     h
                     (recur t)))))]
    (+ (orbit-lenght orbits start root)
       (orbit-lenght orbits end root))))

(-> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L\nK)YOU\nI)SAN"
    (str->orbits))

(-> "COM)B\nB)C\nC)D\nD)E\nE)F\nB)G\nG)H\nD)I\nE)J\nJ)K\nK)L\nK)YOU\nI)SAN"
    (str->orbits)
    (nb-transferts :YOU :SAN))

(-> "input"
    (slurp)
    (str->orbits)
    (nb-transferts :YOU :SAN))
