(use 'clojure.java.io)

(def claim-pattern #"#(\d+) @ (\d+),(\d+): (\d+)x(\d+)")

(defn cart-prod [seq1 seq2]
  ;; the cartesian product of two sequences
  (for [x seq1
        y seq2]
    (list x y)))

(defn claim [line]
  ;; create a claim from a string matching `claim-pattern`
  (let [[_ id x y w h] (re-matches claim-pattern line)]
    {:id (read-string id)
     :x (read-string x) :y (read-string y)
     :w (read-string w) :h (read-string h)}))

(defn claim-coords [claim]
  ;; get the coords of all the patches of a claim
  (cart-prod (range (get claim :x)
                    (+ (get claim :x)
                       (get claim :w)))

             (range (get claim :y)
                    (+ (get claim :y)
                       (get claim :h)))))

(defn add-claim [fabric claim]
  ;; add a claim to a fabric
  ;; (a fabric is just a dict of coords -> nb patches)
  (reduce
   (fn [fabric patch] (update fabric patch #(if (nil? %) 1 (+ % 1))))
   fabric
   (claim-coords claim)))


(defn part1 []
  (with-open [r (reader "./input")]
    (->> r
         line-seq
         (map claim)
         (reduce add-claim {})
         vals
         (filter #(> % 1))
         count)))

(println (part1))

(defn max-overlap [fabric claim]
  ;; get the max overlapping among all patches of a claim with a fabric
  (apply max (map #(get fabric % 0)
                  (claim-coords claim))))

(defn part2 []
  (with-open [r (reader "./input")]
    (let [claims (->> r
                      line-seq
                      (map claim))
          claimed-fabric (reduce add-claim {} claims)]

      (->> claims
           (filter #(= (max-overlap claimed-fabric %) 1))))))

(println (part2))
