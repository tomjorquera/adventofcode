(use 'clojure.java.io)

(def gameresult-pattern #"(\d+) players; last marble is worth (\d+) points")

(defn result [s]
  (let  [[_ players last-marble-score] (re-matches gameresult-pattern s)]
    {:players (read-string players)
     :last-marble-score (read-string last-marble-score)}))

;; turn: Int
;; current: Int
;; state: Vec[Int]
;; nb-players: Int
;; score: Vec[Int]
(defrecord Game [turn current state nb-players score])

(defn insert-at [s i x]
  (apply conj (subvec s 0 i) x (subvec s i)))

(defn remove-at [s i]
  (apply conj (subvec s 0 (- i 1)) (subvec s i)))

(defn index-clockwise [s i n]
  (let [size (count s)
        index (mod (+ i n) size)]
    index))

(defn index-counterclockwise [s i n]
  (let [size (count s)
        index (mod (- i n) size)]
    index))

(defn step [game]
  (let [turn (+ (:turn game) 1)
        state (:state game)
        current (:current game)
        nb-players (:nb-players game)] 
    (if (not= (mod turn 23) 0)
      (let [
            index2 (index-clockwise state current 2)
            new-index (if (= index2 0)
                        (count state)
                        index2 )]

        (->> game
             (#(update % :turn (fn [_] turn)))
             (#(update % :current (fn [_] new-index)))
             (#(update % :state (fn [state] (insert-at (vec state) new-index turn))))))


      (let [score (:score game)
            player (mod turn nb-players)
            removed (index-counterclockwise state current 7)
            removed-value (nth state removed)
            turn-score (+ turn removed-value)
            new-current removed]

        (->> game
             (#(update % :turn (fn [_] turn)))
             (#(update % :current (fn [_] new-current)))
             (#(update % :state (fn [state] (remove-at state removed))))
             (#(update-in % [:score player] (fn [score] (+ (if (= score nil) 0 score) turn-score)))))))))

(defn winner [game]
  (apply max-key val (:score game)))

(defn play-until [game pred]
  (loop [game game]
    (if (pred game)
      game
      (recur (step game)))))

(defn part1 []
  (let [result (with-open [r (reader "./input")]
                 (->> r
                      line-seq
                      first
                      result))] 
    ;; (play-until (Game. 0 0 [0] (:players result) {})
    ;;             (fn [game] (= (:turn game) (:last-marble-score result))))))

    (winner (play-until (Game. 0 0 [0] (:players result) {})
                        (fn [game] (= (:turn game) (:last-marble-score result)))))))

;; (println (part))


(defn part2 []
  (let [result (with-open [r (reader "./input")]
                 (->> r
                      line-seq
                      first
                      result))] 

    (winner (play-until (Game. 0 0 [0] (:players result) {})
                        (fn [game] (= (:turn game) (* 100 (:last-marble-score result))))))))

(println (part2))
