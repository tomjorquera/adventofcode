(require '[clojure.string :as string])

(defrecord Op [code
               modes])

(defrecord Machine [memory
                    pointer
                    stopped
                    input
                    output])

(defn string->intcode [input]
  (->> (string/split input #",")
       (map #(Integer/parseInt %))
       (vec)))

(def ops-alu {1 +
              2 *})

(def mode {0 :memory
           1 :immediate})

(def valid-ops [#"(?:(0?)(1?)([01])0)?(1)"
                #"(?:(0?)(1?)([01])0)?(2)"
                #"(?:(0?)0)?(3)"
                #"(?:(1?)0)?(4)"
                #"(?:(1?)([01])0)?(5)"
                #"(?:(1?)([01])0)?(6)"
                #"(?:(0?)(1?)([01])0)?(7)"
                #"(?:(0?)(1?)([01])0)?(8)"
                #"(99)"])

(defn decode [opcode]
  (let [textcode (str opcode)] 
    (loop [candidates valid-ops]
      (if-not candidates
        nil
        (let [candidate (first candidates)
              candidates (rest candidates)
              matching (re-matches candidate textcode)]
          (if-not matching
            (recur candidates)
            (let [matching (reverse (rest matching))
                  code (Integer/parseInt (first matching))
                  modes (->> (rest matching)
                             (map (fn [x] (if (or (nil? x)
                                                  (= x "")) "0" x)))
                             (map #(Integer/parseInt %))
                             (map mode)
                             vec)]

              (->Op code modes))))))))

(defn compute [op v1 v2]
  (let [opf (get ops-alu op)]
    (opf v1 v2)))

(defn vector-grow [vec n]
  (if (>= (count vec) n)
    vec
    (vector-grow (conj vec 0) n)))

(defn machine-store [machine k v]
  (let [memory (get machine :memory)
        memory (vector-grow memory k)]
    (->Machine (assoc memory k v)
               (get machine :pointer)
               (get machine :stopped)
               (get machine :input)
               (get machine :output))))

(defn machine-read [machine k]
  (-> (get machine :memory)
      (get k)))

(defn machine-read-input [machine]
  [(->Machine (get machine :memory)
              (get machine :pointer)
              (get machine :stopped)
              (rest (get machine :input))
              (get machine :output))
   (first (get machine :input))])

(defn machine-write-output [machine output]
  (->Machine (get machine :memory)
             (get machine :pointer)
             (get machine :stopped)
             (get machine :input)
             (conj (get machine :output) output)))

(defn machine-stop [machine]
  (->Machine (get machine :memory)
             (get machine :pointer)
             true
             (get machine :input)
             (get machine :output)))

(defn machine-set-pointer [machine pointer]
  (->Machine (get machine :memory)
             pointer
             (get machine :stopped)
             (get machine :input)
             (get machine :output)))

(defn machine-increase-pointer [machine & {:keys [incr] :or {incr 1}}]
  (machine-set-pointer machine
                       (+ (get machine :pointer) incr)))

(defn op-alu [machine f modes]
  (let [pointer (get machine :pointer)
        i1 (machine-read machine (+ pointer 1))
        i2 (machine-read machine (+ pointer 2))
        out (machine-read machine (+ pointer 3))
        v1 (if (= (get modes 0) :immediate) i1 (machine-read machine i1))
        v2 (if (= (get modes 1) :immediate) i2 (machine-read machine i2))]
    (-> (machine-store machine out (f v1 v2))
        (machine-increase-pointer :incr (+ (count modes) 1)))))

(defn op-read-input [machine]
  (let [[machine input] (machine-read-input machine)
        pointer (get machine :pointer)]
    (-> (machine-store machine (machine-read machine (+ pointer 1)) input)
        (machine-increase-pointer :incr 2))))

(defn op-write-output [machine modes]
  (let [pointer (get machine :pointer)
        in (machine-read machine (+ pointer 1))
        vin (if (= (get modes 0) :immediate) in (machine-read machine in))]
    (-> (machine-write-output machine vin)
        (machine-increase-pointer :incr (+ (count modes) 1)))))

(defn op-jump-if [machine modes & {:keys [cnd] :or {cnd (fn [x] (not= x 0))}}]
  (let [pointer (get machine :pointer)
        in (machine-read machine (+ pointer 1))
        out (machine-read machine (+ pointer 2))
        vin (if (= (get modes 0) :immediate) in (machine-read machine in))
        vout (if (= (get modes 1) :immediate) out (machine-read machine out))]
    (if (cnd vin)
      (machine-set-pointer machine vout)
      (machine-increase-pointer machine :incr (+ (count modes) 1)))))

(defn op-store-if [machine modes & {:keys [cnd] :or {cnd (fn [x y] (= x y))}}]
  (let [pointer (get machine :pointer)
        i1 (machine-read machine (+ pointer 1))
        i2 (machine-read machine (+ pointer 2))
        out (machine-read machine (+ pointer 3))
        v1 (if (= (get modes 0) :immediate) i1 (machine-read machine i1))
        v2 (if (= (get modes 1) :immediate) i2 (machine-read machine i2))]
    (-> (if (cnd v1 v2)
          (machine-store machine out 1)
          (machine-store machine out 0))
        (machine-increase-pointer :incr (+ (count modes) 1)))))

(defn op-stop [machine]
  (machine-stop machine))

(defn machine-step [machine]
  (let [pointer (get machine :pointer)
        op (->> (machine-read machine pointer)
                (decode))
        machine (case (get op :code)
                  1 (op-alu machine + (get op :modes))
                  2 (op-alu machine * (get op :modes))
                  3 (op-read-input machine)
                  4 (op-write-output machine (get op :modes))
                  5 (op-jump-if machine (get op :modes))
                  6 (op-jump-if machine (get op :modes) :cnd (fn [x] (= x 0)))
                  7 (op-store-if machine (get op :modes) :cnd (fn [x y] (< x y)))
                  8 (op-store-if machine (get op :modes))
                  99 (op-stop machine))]
    machine))

(defn run [intcode input & {:keys [debug] :or {debug false}}]
  (loop [machine (->Machine intcode 0 false input [])]
    (if (get machine :stopped)
      machine
      (recur (do
               (when debug (println machine))
               (machine-step machine))))))

;;; REPL

(decode 1)

(decode 107)

(decode 1005)

(decode 1008)

(->> "1,1,1,4,99,5,6,0,99"
     (string->intcode)
     (#(run % [1])))

(->> (slurp "input")
     (string->intcode)
     (#(run % [1] :debug true)))

;; (-> "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
;;     (string->intcode)
;;     (run [7]))

;; (-> "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
;;     (string->intcode)
;;     (run [8]))

;; (-> "3,9,8,9,10,9,4,9,99,-1,8"
;;     (string->intcode)
;;     (run [8]))

;; (-> "3,9,7,9,10,9,4,9,99,-1,8"
;;     (string->intcode)
;;     (run [8]))

;; (-> "3,3,1108,-1,8,3,4,3,99"
;;     (string->intcode)
;;     (run [8]))

;; (-> "3,3,1107,-1,8,3,4,3,99"
;;     (string->intcode)
;;     (run [8]))

;; (-> "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
;;     (string->intcode)
;;     (run [9]))

(->> (slurp "input")
     (string->intcode)
     (#(run % [5] :debug true)))

