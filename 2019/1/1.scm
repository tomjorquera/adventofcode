(use-modules (ice-9 textual-ports))

(define modules (map string->number (string-split
                                     (call-with-input-file "input" get-string-all)
                                     #\newline)))

(define (fuel-consumption module)
  (- (floor (/ module 3)) 2))

(display
 (apply +
        (map fuel-consumption
             modules)))
(newline)
