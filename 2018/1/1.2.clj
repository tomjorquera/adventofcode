(use 'clojure.java.io)

(with-open [r (reader "./input")]

  (let [deltas (->> r
                    line-seq
                    (map #(Integer/parseInt %))
                    cycle)]

    (defn find-repeat [deltas]
      (loop [freq 0
             deltas deltas
             seen #{}]
        (let [nfreq (+ freq (first deltas))]
          (if (contains? seen nfreq)
            nfreq
            (recur nfreq (rest deltas) (conj seen nfreq))))))

    (print (find-repeat deltas))))
