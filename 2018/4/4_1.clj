(use 'clojure.java.io)
(import java.time.temporal.ChronoUnit)
(import java.time.LocalDateTime)

(def event-pattern #"\[(\d\d\d\d)-(\d\d)-(\d\d) (\d\d):(\d\d)\] (.+)")

(def shift-pattern #"Guard #(\d+) begins shift")
(def asleep-pattern #"falls asleep")
(def wakeup-pattern #"wakes up")

(defn event-type [s]
  (if (re-matches asleep-pattern s)
    {:event :asleep}
    (if (re-matches wakeup-pattern s)
      {:event :wakeup}
      {:event :newshift :id (read-string (nth (re-matches shift-pattern s) 1))})))

(defn event [s]
  (let [[_ year month day hour min content] (re-matches event-pattern s)
        year (read-string (str "10r" year))
        month (read-string (str "10r" month))
        day (read-string (str "10r" day))
        hour (read-string (str "10r" hour))
        min (read-string (str "10r" min))]
    (merge {:year year :month month :day day :hour hour :min min
            :time (LocalDateTime/of year month day hour min)}
           (event-type content))))

(defn minutes [dateTime]
  (+ (* (.getHour dateTime) 60)
     (.getMinute dateTime)))

(defn shifts [stream]
  (if (= stream ())
    []
    (let [shift-start (first stream)
          [shift-content stream] (split-with #(not= (get % :event) :newshift)
                                             (rest stream))]
      (apply conj [(apply conj [shift-start] shift-content)]
             (shifts stream)))))



(defn sleep-table [shift]
  {:id (get (first shift) :id)
   :begin (get (first shift) :time)
   :sleep (loop [table {}
                 events (rest shift)
                 last-event (first shift)]
            (if (= events ())
              table
              (let [new-event (first events)
                    events (rest events)
                    sleep? (if (= (get last-event :event) :asleep) 1 0)]
                (recur
                 (reduce
                  (fn [table min] (update table min #(if (nil? %) sleep? (+ % sleep?))))
                  table
                  (range (minutes (get last-event :time))
                         (minutes (get new-event :time))))
                 events
                 new-event))))})

(defn map-values [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn sleepiest-guard [guards-sleepytimes]
  (key (apply max-key val (map-values #(apply + (vals %)) guards-sleepytimes))))

(defn sleepiest-min [guard-sleepytimes]
  (key (apply max-key val guard-sleepytimes)))


(with-open [r (reader "./input")]
  (let [guards-sleepytimes (->> r
                                line-seq
                                (map event)
                                (sort-by (juxt :year :month :day :hour :min))
                                shifts
                                (map sleep-table)
                                (group-by :id)
                                (map-values (fn [sleep-tables] (map #(get % :sleep )
                                                                    sleep-tables)))
                                (map-values (fn [sleeps] (reduce #(merge-with + %1 %2)
                                                                 {} sleeps))))

        sleepiest (sleepiest-guard guards-sleepytimes)
        sleepiest-guard-sleepiest-min (sleepiest-min (get guards-sleepytimes sleepiest))
        ]
    (println (* sleepiest sleepiest-guard-sleepiest-min))))
