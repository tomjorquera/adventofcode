(use-modules (ice-9 match)
             (ice-9 textual-ports)
             (ice-9 vlist)
             (srfi srfi-1)
             (srfi srfi-11))


;;;; UTILS
 (define (vector-grow v k)
   (let* ((n (vector-length v))
          (n2 (max n k))
          (v2 (make-vector n2 0)))
     (do ((i 0 (+ i 1)))
         ((>= i n) v2)
       (vector-set! v2 i (vector-ref v i)))))
;;;;

(define (string->intcode input)
  (list->vector (map string->number (string-split input #\,))))

(define intcode (string->intcode (call-with-input-file "input" get-string-all)))

(define opcodes (list (list 1 +)
                      (list 2 *)))

(define (compute op i1 i2)
  (let ([opf (second (assq op opcodes))])
    (opf i1 i2)))

(define (store data k v)
  (let ((data-n (vector-grow data (+ k 1))))
    (vector-set! data-n k v)
    data-n))

(define* (step intcode #:optional (pointer 0))
  ;; returns (values intcode pointer stop?)
  (let ((op (vector-ref intcode pointer)))
    (cond
     ((= op 99) (values intcode pointer #t))
     (else (let* ((i1 (vector-ref intcode (+ pointer 1)))
                  (i2 (vector-ref intcode (+ pointer 2)))
                  (out (vector-ref intcode (+ pointer 3)))
                  (v1 (vector-ref intcode i1))
                  (v2 (vector-ref intcode i2))
                  (intcode-new (store intcode out (compute op v1 v2))))
             (values intcode-new (+ pointer 4) #f))))))

(define* (run intcode #:optional (pointer 0) (stop? #f))
  (cond
   ((not stop?) (let-values (((intcode-new pointer-new stop?) (step intcode pointer)))
                  (run intcode-new pointer-new stop?)))
   (else intcode)))

(vector-set! intcode 1 12)
(vector-set! intcode 2 2)

(display (vector-ref (run intcode) 0))
(newline)

(let ((stop? #f)
      (noun 0))
  (while (and (< noun 100)
              (not stop?))
    (let ((verb 0))
      (while (and (< verb 100)
                  (not stop?))

        (vector-set! intcode 1 noun)
        (vector-set! intcode 2 verb)

        (cond
         ((= 19690720 (vector-ref (run intcode) 0))
          (display noun)
          (newline)
          (display verb)
          (newline)
          (set! stop? #t))
         (else
          (set! verb (1+ verb))))))
    (set! noun (1+ noun))))
