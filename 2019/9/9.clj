(require '[clojure.string :as string])
(require '[clojure.edn :as edn])

(defrecord Op [code
               modes])

(defrecord Machine [memory
                    pointer
                    stopped
                    input
                    output
                    relative-base])

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
      (get k)
      ((fn [k](if (nil? k)
                0
                k)))))

(defn mread [machine value mode]
  (case mode
    :immediate value
    :memory (load machine value)
    :relative (load machine (+ value (:relative-base machine)))))

(defn mwrite [machine k mode v]
  (case mode
    :memory (store machine k v)
    :relative (store machine (+ k (:relative-base machine)) v)))

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

(defn adjust-base [machine val]
  (assoc machine :relative-base (+ (:relative-base machine)
                                   val)))

(defn string->intcode [input]
  (->> input
       (string/trim)
       (#(string/split % #","))
       (map #(edn/read-string %))
       (vec)))

(def ops-alu {1 +
              2 *})

(def mode {0 :memory
           1 :immediate
           2 :relative})

(def valid-ops [#"(?:([02]??)([012]??)([012])0)?(1)"
                #"(?:([02]??)([012]??)([012])0)?(2)"
                #"(?:([02]??)0)?(3)"
                #"(?:([012]??)0)?(4)"
                #"(?:([012]??)([012])0)?(5)"
                #"(?:([012]??)([012])0)?(6)"
                #"(?:([02]??)([012]??)([012])0)?(7)"
                #"(?:([02]??)([012]??)([012])0)?(8)"
                #"(?:([012]?)0)?(9)"
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
                  code (edn/read-string (first matching))
                  modes (->> (rest matching)
                             (map (fn [x] (if (or (nil? x)
                                                  (= x "")) "0" x)))
                             (map #(edn/read-string %))
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
        v1 (mread machine i1 (get modes 0))
        v2 (mread machine i2 (get modes 1))]
    (-> (mwrite machine out (get modes 2) (f v1 v2))
        (increase-pointer (+ (count modes) 1)))))

(defn op-io-read [machine modes]
  (let [[machine input] (read-input machine)
        pointer (:pointer machine)
        out (load machine (+ pointer 1))]
    (-> (mwrite machine out (get modes 0) input)
        (increase-pointer 2))))

(defn op-io-write [machine modes]
  (let [pointer (:pointer machine)
        in (load machine (+ pointer 1))
        vin (mread machine in (get modes 0))]
    (-> (write-output machine vin)
        (increase-pointer (+ (count modes) 1)))))

(defn op-jump-if [machine modes & {:keys [cnd] :or {cnd (fn [x] (not= x 0))}}]
  (let [pointer (:pointer machine)
        in (load machine (+ pointer 1))
        out (load machine (+ pointer 2))
        vin (mread machine in (get modes 0))
        vout (mread machine out (get modes 1))]
    (if (cnd vin)
      (set-pointer machine vout)
      (increase-pointer machine (+ (count modes) 1)))))

(defn op-flag-if [machine modes & {:keys [cnd] :or {cnd (fn [x y] (= x y))}}]
  (let [pointer (:pointer machine)
        i1 (load machine (+ pointer 1))
        i2 (load machine (+ pointer 2))
        out (load machine (+ pointer 3))
        v1 (mread machine i1 (get modes 0))
        v2 (mread machine i2 (get modes 1))]
    (-> (if (cnd v1 v2)
          (mwrite machine out (get modes 2) 1)
          (mwrite machine out (get modes 2) 0))
        (increase-pointer (+ (count modes) 1)))))

(defn op-stop [machine]
  (stop machine))

(defn op-adjust-base [machine modes]
  (let [pointer (:pointer machine)
        in (load machine (+ pointer 1))
        vin (mread machine in (get modes 0))]
    (-> machine
        (adjust-base vin)
        (increase-pointer (+ (count modes) 1)))))

(defn step [machine]
  (let [pointer (:pointer machine)
        op (->> (load machine pointer)
                (decode))
        machine (case (:code op)
                  1 (op-alu machine + (:modes op))
                  2 (op-alu machine * (:modes op))
                  3 (op-io-read machine (:modes op))
                  4 (op-io-write machine (:modes op))
                  5 (op-jump-if machine (:modes op))
                  6 (op-jump-if machine (:modes op) :cnd (fn [x] (= x 0)))
                  7 (op-flag-if machine (:modes op) :cnd (fn [x y] (< x y)))
                  8 (op-flag-if machine (:modes op))
                  9 (op-adjust-base machine (:modes op))
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
  (let [machine (->Machine intcode 0 false input [] 0)]
    (step-until machine :debug debug)))


;;; DISASSEMBLY

(defn trace-op [machine op]
  (let [pointer (:pointer machine)
        params (map #(load machine %) (range (+ pointer 1)
                                             (+ pointer (count (:modes op)) 1)))
        values (map #(apply (fn [param mode] (mread machine param mode)) %)
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
  (let [machine (->Machine intcode 0 false [] [] 0)]
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
  (let [machine (->Machine intcode 0 false input [] 0)]
    (lazy-trace-machine machine)))

(defn pretty-print-param [param mode value]
  (case mode
    :immediate (str param)
    :memory (str "&" param "(" value ")")
    :relative (str "+&"param "(" value ")")))

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
           9 (str "adjbase " pp-params)
           99 "halt")
         "\n")))

;;; SOL

(->> "input"
     slurp
     string->intcode
     (#(run % [1])))

(->> "input"
     slurp
     string->intcode
     (#(run % [2])))

;;; REPL

(->> "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99"
     string->intcode
     (#(trace % []))
    (map #(apply pretty-print %))
     (string/join)
     println)

(->> "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99"
     string->intcode
     (#(run % [])))

(->> "1102,34915192,34915192,7,4,7,99,0"
     string->intcode
     (#(run % [])))

(->> "104,1125899906842624,99"
     string->intcode
     (#(run % [])))

(->> "input"
     slurp
     string->intcode
     (#(trace % [2]))
     (map #(apply pretty-print %))
     (string/join)
     println)


(->> "input"
     slurp
     string->intcode
     (#(disassemble %))
     (map #(apply pretty-print %))
     (string/join)
     println)
