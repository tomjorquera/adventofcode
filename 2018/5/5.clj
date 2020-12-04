(use 'clojure.java.io)

(defn reactants? [c1 c2]
  (and (not= c1 c2)
       (= (clojure.string/lower-case c1)
          (clojure.string/lower-case c2))))


(defn react-once [s]
  (loop [acc ""
         last_unit ""
         s s]
    (if (= s ())
      (str acc last_unit)
      (let [new_unit (first s)
            next_units (rest s)]
        (if (reactants? last_unit new_unit)
          (recur acc "" next_units)
          (recur (str acc last_unit) new_unit next_units))))))

(defn react [s]
  (loop [old_s s
         new_s (react-once s)]
    (if (= old_s new_s)
      old_s
      (recur new_s (react-once new_s)))))

(defn part1 []
  (with-open [r (reader "./input")]
    (->> r
         line-seq
         first
         react
         count)))

(defn drop-reactant [sequence c]
  (apply str (filter (fn [x] (and (not= (str c) (str x))
                                  (not= (str c) (clojure.string/lower-case (str x))))) 
                     sequence)))

(def candidates "abcdefghijklmnopqrstuvwxyz")

(defn part2 []
  (with-open [r (reader "./input")]
    (->> r
         line-seq
         first
         (#(apply hash-map (interleave candidates
                                       (map (fn [c] (count (react (drop-reactant % c)))) candidates))))
         (apply min-key val))))

(println (part1))

(println (part2))
