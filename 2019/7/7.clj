(require '[clojure.string :as string])

(defrecord Op [code
               modes])

(defrecord Machine [memory
                    pointer
                    stopped
                    input
                    output])

(defn string->intcode [input]
  (->> input
       (string/trim)
       (#(string/split % #","))
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

(defn machine-append-input [machine in]
  (->Machine (get machine :memory)
              (get machine :pointer)
              (get machine :stopped)
              (conj (get machine :input) in)
              (get machine :output)))

(defn machine-write-output [machine output]
  (->Machine (get machine :memory)
             (get machine :pointer)
             (get machine :stopped)
             (get machine :input)
             (conj (get machine :output) output)))

(defn machine-clear-output [machine]
  (->Machine (get machine :memory)
             (get machine :pointer)
             (get machine :stopped)
             (get machine :input)
             []))

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

(defn step-until [machine & {:keys [cnd debug] :or {cnd (fn [_] false)
                                                    debug false}}]
  (loop [machine machine]
    (if (or (cnd machine)
            (get machine :stopped))
      machine
      (recur (do
               (when debug (println machine))
               (machine-step machine))))))

(defn run [intcode input & {:keys [debug] :or {debug false}}]
  (let [machine (->Machine intcode 0 false input [])]
    (step-until machine :debug debug)))

;;; PART 1

(defn comp-thrust [amp-prog phase-setting]
  (let [res (loop [settings phase-setting
                   input [0]]
              (cond
                (empty? settings) input
                :else (let [[setting & rest] settings
                            m (run amp-prog (into [setting] input))
                            out (get m :output)]
                        (recur rest out))))]
    (-> res
        (string/join)
        (Integer.))))

(defn permutations [colls]
  (map vec
       (if (= 1 (count colls))
         (list colls)
         (for [head colls
               tail (permutations (disj (set colls) head))]
           (cons head tail)))))

(let [candidates (permutations [0 1 2 3 4])
      amp-code (-> "input"
                        (slurp)
                        (string->intcode))
      best-candidate (apply max-key #(comp-thrust amp-code %) candidates)]
  [best-candidate (comp-thrust amp-code best-candidate)])

;;; PART 2

(defn thruster-sequence [thrusters initial-input]
  (loop [machines-in thrusters
         machines-out []
         input initial-input]
    (if (empty? machines-in)
      [machines-out input]
      (let [machine (first machines-in)
            machines-in (rest machines-in)
            machine (machine-append-input machine input)
            machine (step-until machine :cnd (fn [m] (not (empty? (get m :output))))
                                :debug true)
            out (first (get machine :output))
            machine (machine-clear-output machine)]
        (recur machines-in (conj machines-out machine) out)))))

(defn thruster-loop [amp-prog phase-setting]
  (let [amps (->> phase-setting
                  (map #(->Machine amp-prog 0 false [%] [])))]
    (loop [amps amps
           input 0]
      (let [[amps output] (thruster-sequence amps input)]
        (if (nil? output)
          input
          (recur amps output))))))

(let [candidates (permutations [5 6 7 8 9])
      amp-code (-> "input"
                   (slurp)
                   (string->intcode))
      best-candidate (apply max-key #(thruster-loop amp-code %) candidates)]
  [best-candidate (thruster-loop amp-code best-candidate)])

;;; REPL

(run (-> "input"
         (slurp)
         (string->intcode))
  [0 18125]
  :debug true)

(comp-thrust (-> "3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0"
                 (string->intcode))
             [4 3 2 1 0])


(comp-thrust (-> "3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0"
                 (string->intcode))
             [0 1 2 3 4])

(comp-thrust (-> "3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0"
                 (string->intcode))
             [1 0 4 3 2])

(comp-thrust (-> "input" (slurp) (string->intcode))
             [4 3 2 1 0])

(->> [0 1 2]
     permutations
     (apply max-key #(comp-thrust (string->intcode "3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0") %)))

(step-until (->Machine (-> "input" (slurp) (string->intcode))
                       0
                       false
                       [5 1]
                       [])
            :cnd (fn [m] (not (empty? (get m :output)))))

(->> [0 1 2]
     (map #(->Machine (-> "input" (slurp) (string->intcode))
                      0 false [%] []))
     (#(thruster-sequence % 0)))

(-> "3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5"
    (string->intcode)
    (thruster-loop [9 8 7 6 5]))

(-> "3,52,1001,52,-5,52,3,53,1,52,56,54,1007,54,5,55,1005,55,26,1001,54,-5,54,1105,1,12,1,53,54,53,1008,54,0,55,1001,55,1,55,2,53,55,53,4,53,1001,56,-1,56,1005,56,6,99,0,0,0,0,10" (string->intcode)
    (thruster-loop [9 7 8 5 6]))

(thruster-loop (-> "input" (slurp) (string->intcode))
               [9 8 7 6 5])
