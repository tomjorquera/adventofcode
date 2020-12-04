(require '[clojure.string :as string])

(defrecord Op [code
               modes])

(defrecord Machine [memory
                    pointer
                    stopped
                    input
                    output])

(defn vector-grow [vec n]
  (if (>= (count vec) n)
    vec
    (vector-grow (conj vec 0) n)))

(defn store [machine k v]
  (let [memory (:memory machine)
        memory (vector-grow memory k)]
    (assoc machine :memory (assoc memory k v))))

(defn load [machine k]
  (-> (:memory machine)
      (get k)))

(defn read-input [machine]
  [(assoc machine :input (rest (:input machine)))
    (first (:input machine))])

(defn append-input [machine in]
  (assoc machine :input (conj (:input machine) in)))

(defn write-output [machine out]
  (assoc machine :output (conj (:output machine) out)))

(defn clear-output [machine]
  (assoc machine :output []))

(defn stop [machine]
  (assoc machine :stopped true))

(defn set-pointer [machine pointer]
  (assoc machine :pointer pointer))

(defn increase-pointer [machine incr]
  (set-pointer machine
               (+ (:pointer machine) incr)))

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
                #"(?:(0?)

(1?)([01])0)?(8)"
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

(defn op-alu [machine f modes]
  (let [pointer (:pointer machine)
        i1 (load machine (+ pointer 1))
        i2 (load machine (+ pointer 2))
        out (load machine (+ pointer 3))
        v1 (if (= (get modes 0) :immediate) i1 (load machine i1))
        v2 (if (= (get modes 1) :immediate) i2 (load machine i2))]
    (-> (store machine out (f v1 v2))
        (increase-pointer (+ (count modes) 1)))))

(defn op-io-read [machine]
  (let [[machine input] (read-input machine)
        pointer (:pointer machine)]
    (-> (store machine (load machine (+ pointer 1)) input)
        (increase-pointer 2))))

(defn op-io-write [machine modes]
  (let [pointer (:pointer machine)
        in (load machine (+ pointer 1))
        vin (if (= (get modes 0) :immediate) in (load machine in))]
    (-> (write-output machine vin)
        (increase-pointer (+ (count modes) 1)))))

(defn op-jump-if [machine modes & {:keys [cnd] :or {cnd (fn [x] (not= x 0))}}]
  (let [pointer (:pointer machine)
        in (load machine (+ pointer 1))
        out (load machine (+ pointer 2))
        vin (if (= (get modes 0) :immediate) in (load machine in))
        vout (if (= (get modes 1) :immediate) out (load machine out))]
    (if (cnd vin)
      (set-pointer machine vout)
      (increase-pointer machine (+ (count modes) 1)))))

(defn op-flag-if [machine modes & {:keys [cnd] :or {cnd (fn [x y] (= x y))}}]
  (let [pointer (:pointer machine)
        i1 (load machine (+ pointer 1))
        i2 (load machine (+ pointer 2))
        out (load machine (+ pointer 3))
        v1 (if (= (get modes 0) :immediate) i1 (load machine i1))
        v2 (if (= (get modes 1) :immediate) i2 (load machine i2))]
    (-> (if (cnd v1 v2)
          (store machine out 1)
          (store machine out 0))
        (increase-pointer (+ (count modes) 1)))))

(defn op-stop [machine]
  (stop machine))

(defn step [machine]
  (let [pointer (:pointer machine)
        op (->> (load machine pointer)
                (decode))
        machine (case (:code op)
                  1 (op-alu machine + (:modes op))
                  2 (op-alu machine * (:modes op))
                  3 (op-io-read machine)
                  4 (op-io-write machine (:modes op))
                  5 (op-jump-if machine (:modes op))
                  6 (op-jump-if machine (:modes op) :cnd (fn [x] (= x 0)))
                  7 (op-flag-if machine (:modes op) :cnd (fn [x y] (< x y)))
                  8 (op-flag-if machine (:modes op))
                  99 (op-stop machine))]
    machine))

(defn step-until [machine & {:keys [cnd debug] :or {cnd (fn [_] false)
                                                    debug false}}]
  (loop [machine machine]
    (if (or (cnd machine)
            (:stopped machine))
      machine
      (recur (do
               (when debug (println machine))
               (step machine))))))

(defn run [intcode input & {:keys [debug] :or {debug false}}]
  (let [machine (->Machine intcode 0 false input [])]
    (step-until machine :debug debug)))


; DISASSEMBLY

(defn trace-op [machine op]
  (let [pointer (:pointer machine)
        params (map #(load machine %) (range (+ pointer 1)
                                             (+ pointer (count (:modes op)) 1)))
        values (map #(apply (fn [param mode] (if (= mode :immediate) param (load machine param))) %)
                    (map vector params (:modes op)))]
    [op params values]))

(defn lazy-disassemble-machine [machine]
  (if (:stopped machine)
    nil
    (let [pointer (:pointer machine)
          op (->> (load machine pointer)
                  (decode))
          op-trace (trace-op machine op)
          pointer (+ pointer (count (:modes op)) 1)
          machine (set-pointer machine pointer)
          machine (if (= (:code op ) 99) (stop machine) machine)]
      (lazy-seq (cons op-trace (lazy-disassemble-machine machine))))))

(defn disassemble [intcode]
  (let [machine (->Machine intcode 0 false [] [])]
    (lazy-disassemble-machine machine)))

(defn lazy-trace-machine [machine]
  (if (:stopped machine)
    nil
    (let [pointer (:pointer machine)
          op (->> (load machine pointer)
                  (decode))
          op-trace (trace-op machine op)
          machine (step machine)]
      (lazy-seq (cons op-trace (lazy-trace-machine machine))))))

(defn trace [intcode input]
  (let [machine (->Machine intcode 0 false input [])]
    (lazy-trace-machine machine)))

(defn pretty-print-param [param mode value]
  (case mode
    :immediate (str param)
    :memory (str "&" param "(" value ")")))

(defn pretty-print [op params values]
  (let [param-mode (map vector params (:modes op) values)
        pp-params (map #(apply pretty-print-param %) param-mode)
        pp-params (string/join " " pp-params)]
    (str (case (:code op)
           1 (str "+ " pp-params)
           2 (str "* " pp-params)
           3 (str "ioread " pp-params)
           4 (str "iowrite " pp-params)
           5 (str "jumpif!0 " pp-params)
           6 (str "jumpif0 " pp-params)
           7 (str "flagif< " pp-params)
           8 (str "flagif= " pp-params)
           99 "halt")
         "\n")))


;;; REPL

(def machine (->Machine [1 2 2 99] 0 false [-1] [-2]))

(store machine  10 99)

(read-input machine)

(append-input machine -99)

(write-output machine -99)

(clear-output machine)

(stop machine)

(set-pointer machine 99)

(increase-pointer machine 3)

(op-alu machine + [:immediate :immediate :memory])

(op-io-read machine)

(-> "3,3,1108,-1,8,3,4,3,99"
    (string->intcode)
    (run [8]))

(->> "3,3,1108,-1,8,3,4,3,99"
     (string->intcode)
     (#(disassemble %))
     (map #(apply pretty-print %)))

(->> "3,3,1108,-1,8,3,4,3,99"
    (string->intcode)
    (#(trace % [0]))
    (map #(apply pretty-print %)))

(-> "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
    (string->intcode)
    (run [7]))

(->> "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
     (string->intcode)
     (#(disassemble %))
     (map #(apply pretty-print %)))

(->> "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
     (string->intcode)
     (#(trace % [0]))
     (map #(apply pretty-print %)))

(->> "input-2"
     (slurp)
     (string->intcode)
     (#(->Machine % 0 false [] []))
     (#(store % 1 12))
     (#(store % 2 2))
     (lazy-trace-machine)
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-2"
     (slurp)
     (string->intcode)
     (#(->Machine % 0 false [] []))
     (#(store % 1 82))
     (#(store % 2 26))
     (lazy-trace-machine)
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-5"
     (slurp)
     (string->intcode)
     (#(trace % [1]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-5"
     (slurp)
     (string->intcode)
     (#(trace % [5]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-7"
     (slurp)
     (string->intcode)
     (#(trace % [0 0]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-7"
     (slurp)
     (string->intcode)
     (#(trace % [1 5]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-7"
     (slurp)
     (string->intcode)
     (#(trace % [2 140]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-7"
     (slurp)
     (string->intcode)
     (#(trace % [3 145]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

(->> "input-7"
     (slurp)
     (string->intcode)
     (#(trace % [4 2919]))
     (map #(apply pretty-print %))
     (string/join)
     (println))


(->> "input-7"
     (slurp)
     (string->intcode)
     (#(trace % [5 0]))
     (map #(apply pretty-print %))
     (string/join)
     (println))

