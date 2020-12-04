(use 'clojure.java.io)

(def initial-state-regex #"initial state: ([\.#]+)")
(def rule-regex #"([\.#][\.#][\.#][\.#][\.#]) => ([\.#])")


(defn new-state [s]
  (let [[_ state] (re-matches initial-state-regex s)]
    (into (sorted-map) (map-indexed (fn [i v] [i (str v)]) state))))


(defn new-rule [s]
  (let [[_ cond act] (re-matches rule-regex s)]
    [cond act]))

(with-open [r (reader "./input")]
  (let [[state-string _ & rules-strings] (doall (line-seq r))
        init-state (new-state state-string)
        rules (into {} (map new-rule rules-strings))]
    (do
      (def init-state init-state)
      (def rules rules))))

(defn context [state i reach]
  (into (sorted-map) (map (fn [i] [i (get state i ".")])
                          (range (- i reach) (+ i (+ reach 1))))))

(defn new-val [state rules reach i]
  (let [context (context state i reach)]
    (get rules (apply str (map val context)) ".")))

(defn trim [state]
  (let [state (into (sorted-map) (drop-while #(= (val %) ".") state))
        state (loop [state state]
                (let [maxk (apply max (keys state))]
                  (if (not= (get state maxk) ".")
                    state
                    (recur (dissoc state maxk)))))]
    state))

(defn step [state rules reach]
  (let [left-border (key (apply min-key key state))
        right-border (key (apply max-key key state))
        state (into {} (concat state
                               (for [i (range (- left-border reach) left-border)] [i "."])
                               (for [i (range (+ right-border 1) (+ right-border reach 1))] [i "."])))]
    (trim (into (sorted-map) (map (fn [i] [i (new-val state rules reach i)]) (keys state))))))


(defn part1 []
  (do (println (apply str (map val init-state)))
      (->> (reduce (fn [s i]
                     (let [new-s (step s rules 2)]
                       (do
                         (println (apply str (concat [i "\t\t\t" (apply str (map val new-s))])))
                         new-s)))
                   init-state
                   (range 20))
           (filter #(= (val %) "#"))
           (map key)
           (apply + ))))

(defn part2 []
  (do (println (apply str (concat "b\t\t\t" (map val init-state))))
      (->> (reduce (fn [s i]
                     (let [new-s (step s rules 2)]
                       (do
                         (println (apply str (concat [i] "\t\t\t(" [(apply min (map key new-s))] ")\t" (map val new-s))))
                         new-s)))
                   init-state
                   (range 100)) ;; at this point, the pattern simply shifts right by one, so we can simulate all the steps by multiplying by the remaining steps
           (filter #(= (val %) "#"))
           (map key)
           (map #(+ (- 50000000000 100) %))
           (apply +))))

(println (part1))

(println (part2))
