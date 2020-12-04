(require '[clojure.string :as string])

(def input "278384-824795")

(defn str->range [input]
  (->> (string/split input #"-")
       (map #(Integer/parseInt %))
       (apply range)))

(defn valid? [num]
  (let [res (reduce
             (fn [acc n]
               (let [n (Character/getNumericValue n)]
                 {:prev n
                  :increasing? (and (get acc :increasing?)
                                    (>= n (get acc :prev)))
                  :doubled? (or (get acc :doubled?)
                                (= n (get acc :prev)))}))
             {:prev -1
              :increasing? true
              :doubled? false}
             (str num))]

    (and (get res :increasing?)
         (get res :doubled?))))

(->> (str->range input)
     (filter valid?)
     (count))

;;; PART 2

(defn valid2? [num]
  (let [res (reduce
             (fn [acc n]
               (let [n (Character/getNumericValue n)]
                 {:prev n
                  :count (if (= (get acc :prev) n)
                           (+ (get acc :count) 1)
                           0)
                  :increasing? (and (get acc :increasing?)
                                    (>= n (get acc :prev)))
                  :doubled? (or (get acc :doubled?)
                                (and (not (= n (get acc :prev)))
                                     (= (get acc :count) 1)))}))
             {:prev -1
              :count 0
              :increasing? true
              :doubled? false}
             (str num))]

    (and (get res :increasing?)
         (or (get res :doubled?)
             (= (get res :count) 1)))))

(->> (str->range input)
     (filter valid2?)
     (count))

;;; REPL

(str->range "1-5")

(valid? 12232)

(valid? 122334)

(valid2? 112233)

(valid2? 123444)

(valid2? 111122)

(valid2? 122)
