(use 'clojure.java.io)

(defprotocol Movable
  (pos [this])
  (orient [this])
  (dead? [this])
  (crashes? [this cart])
  (turn [this segment])
  (fwd [this])
  (mv [this segment])
  (kill [this]))

(deftype Cart [x y orient next-turn dead?]
  Object Movable

  (toString [this]
    (str [x y orient next-turn dead?]))

  (pos [this]
    [x y])

  (orient [this]
    orient)

  (dead? [this]
    dead?)

  (crashes? [this cart]
    (= (pos cart) [x y]))

  (turn [this segment]
    (let [[new-orient next-turn] (cond
                                   (= [orient segment] [:E \\]) [:S next-turn]
                                   (= [orient segment] [:E \/]) [:N next-turn]
                                   (= [orient segment] [:S \\]) [:E next-turn]
                                   (= [orient segment] [:S \/]) [:W next-turn]
                                   (= [orient segment] [:W \\]) [:N next-turn]
                                   (= [orient segment] [:W \/]) [:S next-turn]
                                   (= [orient segment] [:N \\]) [:W next-turn]
                                   (= [orient segment] [:N \/]) [:E next-turn]
                                   (= segment \+) (cond
                                                    (= [:E :L] [orient next-turn]) [:N :F]
                                                    (= [:E :F] [orient next-turn]) [:E :R]
                                                    (= [:E :R] [orient next-turn]) [:S :L]
                                                    (= [:S :L] [orient next-turn]) [:E :F]
                                                    (= [:S :F] [orient next-turn]) [:S :R]
                                                    (= [:S :R] [orient next-turn]) [:W :L]
                                                    (= [:W :L] [orient next-turn]) [:S :F]
                                                    (= [:W :F] [orient next-turn]) [:W :R]
                                                    (= [:W :R] [orient next-turn]) [:N :L]
                                                    (= [:N :L] [orient next-turn]) [:W :F]
                                                    (= [:N :F] [orient next-turn]) [:N :R]
                                                    (= [:N :R] [orient next-turn]) [:E :L])
                                   :else [orient next-turn])]
      (Cart. x y new-orient next-turn dead?)))

  (fwd [this]
    (let [[new-x new-y] (cond
                          (= orient :E) [(inc x) y]
                          (= orient :S) [x (inc y)]
                          (= orient :W) [(dec x) y]
                          (= orient :N) [x (dec y)])]
      (Cart. new-x new-y orient next-turn dead?)))

  (mv [this segment]
    (->> this
         (#(turn % segment))
         fwd))

  (kill [this]
    (Cart. x y orient next-turn true)))

(defn new-orient [c]
  (cond
    (= c \>) :E
    (= c \v) :S
    (= c \<) :W
    (= c \^) :N))

(defn new-grid [s]
  (let [parse-line (fn [y s]
                     (loop
                         [[[c & rest] line x carts] [s [] 0 []]]

                       (if (= c nil)
                         {:line line :carts carts}

                         (recur (cond
                                  (some #(= c %) [\> \v \< \^]) [rest
                                                                 (conj line \.)
                                                                 (inc x)
                                                                 (conj carts (Cart. x y (new-orient c) :L false))]
                                  :else [rest
                                         (conj line c)
                                         (inc x)
                                         carts]))))) 
        lines (map-indexed parse-line s)]
    (reduce (fn [grid line] (->> grid
                                 (#(update % :map (fn [e] (conj e (:line line)))))
                                 (#(update % :carts (fn [e] (concat e (:carts line)))))))
            {:map [] :carts []}
            lines)))

(defn crash? [grid]
  (some #(> (val %) 1) (frequencies (map pos (:carts grid)))))


(defn road [grid x y]
  (get-in grid [:map y x])) ; careful, the grid is a vec[vec] with y first!

(defn carts-in-order [grid]
  (sort (fn [c1 c2]
          (let [[x1 y1] (pos c1)
                [x2 y2] (pos c2)]
            (if (not= y1 y2)
              (compare y1 y2)
              (compare x1 x2))))
        (:carts grid)))

(defn update-crashed-state [cart carts]
  (loop [cart cart
         [other-cart & todo-carts] carts
         done-carts []]
    (if (= other-cart nil)
      [cart done-carts]
      (let [[cart other-cart] (if (= (pos cart) (pos other-cart))
                                [(kill cart) (kill other-cart)]
                                [cart other-cart])]
        (recur cart todo-carts (conj done-carts other-cart))))))

(defn step [grid]
  (let [carts (carts-in-order grid)]
    (loop [[cart & carts] carts
           moved-carts []]
      (if (= cart nil)
        (update grid :carts (fn [_] moved-carts))
        (let [[x y] (pos cart)
              cart (mv cart (road grid x y))
              [cart carts] (update-crashed-state cart carts)
              [cart moved-carts] (update-crashed-state cart moved-carts)]
          (recur carts (conj moved-carts cart)))))))

(defn gridprint [grid]
  (do (doall (for [l (:map grid)]
               (println (apply str l))))
      (println (:carts grid))))

(defn part1 []
  (with-open [r (reader "./input")]
    (let [grid (->> r
                    line-seq
                    new-grid)]

      (loop [grid grid
             i 0]
        (if (or (crash? grid) (> i 9999))
          [i (filter #(> (val %) 1) (frequencies (map pos (:carts grid))))]
          (recur (step grid) (inc i)))))))

(println (part1))

(defn drop-crashed [grid]
  (update grid :carts (fn [carts] (filter #(not (dead? %)) carts))))

(defn part2 []
  (with-open [r (reader "./input")]
    (let [grid (->> r
                    line-seq
                    new-grid)]

      (loop [grid grid
             i 0]
        (if (or (< (count (:carts grid)) 2) (> i 99999))
          [i (:carts grid)]
          (do (when (or (= (mod i 10000) 0)
                        (crash? (step grid)))
                (println (apply str (concat [(inc i) " "] (:carts (step grid))))))
              (recur (drop-crashed (step grid)) (inc i))))))))

(println (part2))
