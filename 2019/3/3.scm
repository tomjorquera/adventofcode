(use-modules (ice-9 match)
             (ice-9 textual-ports)
             (ice-9 vlist)
             (srfi srfi-1)
             (srfi srfi-9 gnu)
             (srfi srfi-11))

(define (string->step input)
  (list (string-ref input 0)
        (string->number (substring input 1))))

(define (string->path input)
  (map string->step
       (string-split input #\,)))

(define circuits
  (map string->path
       (string-split (call-with-input-file "input" get-string-all)
                     #\newline)))

(define-record-type <circuit-display>
  (make-circuit-display grid xmin xmax ymin ymax)
  circuit-display?
  (grid circuit-display-grid set-circuit-display-grid!)
  (xmin circuit-display-xmin set-circuit-display-xmin!)
  (xmax circuit-display-xmax set-circuit-display-xmax!)
  (ymin circuit-display-ymin set-circuit-display-ymin!)
  (ymax circuit-display-ymax set-circuit-display-ymax!))

(define* (circuit-display-pretty-print record #:optional (port (current-output-port)))
  (do ((y (circuit-display-ymin record) (1+ y)))
      ((> y (circuit-display-ymax record)))
    (do ((x (circuit-display-xmin record) (1+ x)))
        ((> x (circuit-display-ymax record)) (write-char #\newline port))
      (let ((symbol (vhash-assoc (list x y) (circuit-display-grid record))))
        (match symbol
          ((_ (or #\- #\| #\+ #\o)) (write-char (second symbol) port))
          (else (write-char #\. port)))))))

(define (circuit-display-empty)
  (make-circuit-display
   (alist->vhash
    (list (list (list 0 0)  #\o)))
   0 0 0 0))

(define (circuit->circuit-display circuit)
  (let ((circuit-display (circuit-display-empty))
        (x 0)
        (y 0))
    (for-each (lambda (step)
                (match step
                  ((#\U dist)
                   (do ((i 0 (1- i)))
                       ((< i dist))
                     (let ((y-new (- y i)))
                       (set-circuit-display-ymin! circuit-display
                                                  (min y-new (circuit-display-ymin circuit)))
                       (set-circuit-display-grid! circuit-display
                                                  (vhash-cons (list x y-new) #\| (circuit-display-grid circuit-display)))
                       ))
                   (set! y (- y dist)))
                  ((#\D dist) #f)
                  ((#\L dist) #f)
                  ((#\R dist) #f)))
              circuit)
    circuit-display))

(circuit-display-pretty-print (circuit-display-empty))
