(use-modules (ice-9 textual-ports))

(define modules (map string->number (string-split
                                     (call-with-input-file "input" get-string-all)
                                     #\newline)))

(define (fuel-consumption module)
  (let ([fuel (- (floor (/ module 3)) 2)])
    (cond
     ((<= fuel 0) 0)
     (else fuel))))

(define (total-consumption module)
  (cond
   ((<= module 0) 0)
   (else (let ([fuel (fuel-consumption module)])
           (+ fuel (total-consumption fuel))))))

(display
 (apply +
        (map total-consumption
             modules)))
(newline)
