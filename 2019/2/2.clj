(require '[clojure.string :as string])

(defn string->intcode [input]
  (->> (string/split input #",")
       (map #(Integer/parseInt %))
       (vec)))

(def opcodes {1 +
              2 *})

(defn vector-grow [vec n]
  (if (>= (count vec) n)
    vec
    (vector-grow (conj vec 0) n)))

(defn compute [op v1 v2]
  (let [opf (get opcodes op)]
    (opf v1 v2)))

(defn store [data k v]
  (let [data-n (vector-grow data k)]
    (assoc data-n k v)))

(defn step [intcode & {:keys [pointer] :or {pointer 0}}]
  (let [op (get intcode pointer)]
    (case op
      99 [intcode pointer true]
      (let [i1 (get intcode (+ pointer 1))
            i2 (get intcode (+ pointer 2))
            out (get intcode (+ pointer 3))
            v1 (get intcode i1)
            v2 (get intcode i2)
            intcode-new (store intcode out
                               (compute op v1 v2))]
        [intcode-new (+ pointer 4) false]))))

(defn run [intcode & {:keys [pointer stop?] :or {pointer 0
                                                 stop? false}}]
  (if stop? intcode
      (let [[intcode pointer stop?] (step intcode :pointer pointer)]
        (run intcode :pointer pointer :stop? stop?))))

;;; REPL

(string->intcode "1,2,3")

(def intcode
  (->> (slurp "input")
       (string->intcode)))

(->> (slurp "input")
     (string->intcode)
     (run))
