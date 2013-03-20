(ns me.arrdem.imprecise)

(defprotocol TolerancedNumber
  "A protocol for numeric values with error parts"
  (tolerance  [n] "Returns the tolerance of the number.")
  (to-scalar  [n] "Returns the scalar part of the tolerance.")
  (to-enum    [n] "Returns the conversion of the argument to an error number.")
  (tol-min    [n] "Returns the minimum of the tolerance range.")
  (tol-max    [n] "Returns the maximum of the tolerance range."))

(defrecord ENumber [val tol]
  TolerancedNumber
    (tolerance [self] (. self tol))
    (to-scalar [self] (. self val))
    (to-enum   [self] self)
    (tol-min   [self] (clojure.core/- (. self val) (. self tol)))
    (tol-max   [self] (clojure.core/+ (. self val) (. self tol)))

  (toString  [self] (str (.val self) "±" (.tol self))))

(defn e [x y] (ENumber. x y))

(defn e+ [x y]
  (ENumber. (clojure.core/+ (to-scalar x) (to-scalar y))
            (clojure.core/+ (tolerance x) (tolerance y))))

(defn e- [x y]
  (ENumber. (clojure.core/- (to-scalar x) (to-scalar y))
            (clojure.core/+ (tolerance x) (tolerance y))))

(defn e* [x y]
  (ENumber. (clojure.core/* (to-scalar x) (to-scalar y))
            (clojure.core/+
             (clojure.core/* (to-scalar x) (tolerance y))
             (clojure.core/* (to-scalar y) (tolerance x)))))

(defn e-div [x y]
 (ENumber. (clojure.core// (to-scalar x) (to-scalar y))
            (clojure.core/+
             (clojure.core/* (to-scalar x) (tolerance y))
             (clojure.core/* (to-scalar y) (tolerance x)))))

(defmacro multi-extend-type [types & rest]
  (doseq [t types]
    (eval `(extend-type ~t ~@rest))))

(multi-extend-type [java.lang.Number
                    java.math.BigDecimal
                    java.math.BigInteger
                    java.lang.Byte
                    java.lang.Long
                    java.lang.Integer
                    java.lang.Double
                    java.lang.Float
                    java.lang.Short
                    ]
  TolerancedNumber
   (tolerance ([_] 0))
   (to-scalar ([x] x))
   (to-enum   ([x] (ENumber. x 0)))
   (tol-min   ([x] x))
   (tol-max   ([x] x)))

;;------------------------------------------------------------------------------

(defmacro multi-defmethod [sym alt-forms & rest]
  (doseq [f alt-forms]
    (eval `(defmethod ~sym ~f ~@rest))))

(defmacro ensure-enumbers [bindings & forms]
  `(let ~(apply vector
                (reduce (fn [seq sym]
                          (concat seq
                                  (list sym
                                        `(to-enum ~sym))))
                        (list) bindings))
     ~@forms))

;;------------------------------------------------------------------------------

(declare + - / * )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti + (fn [& rest]
              (let [[x y] rest]
                [(type x) (type y)])))

(defmethod + [nil nil] [& rest] 0)
(defmethod + [java.lang.Number nil] [x] x)
(defmethod + [java.lang.Number java.lang.Number] [x y & rest]
   (reduce + (clojure.core/+ x y) rest))

(defmethod + [ENumber nil] [x] x)
(multi-defmethod + [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y & rest]
  (ensure-enumbers [x y]
    (reduce + (e+ x y) rest)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti - (fn [& rest]
              (let [[x y] rest]
                [(type x) (type y)])))

(defmethod - [nil nil] [& rest] 0)
(defmethod - [java.lang.Number nil] [x] x)
(defmethod - [java.lang.Number java.lang.Number] [x y & rest]
   (reduce - (clojure.core/- x y) rest))

(defmethod - [ENumber nil] [x] x)
(multi-defmethod - [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y & rest]
  (ensure-enumbers [x y]
    (reduce - (e- x y) rest)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti * (fn [& rest]
              (let [[x y] rest]
                [(type x) (type y)])))

(defmethod * [nil nil] [& rest] 1)
(defmethod * [java.lang.Number nil] [x] x)
(defmethod * [java.lang.Number java.lang.Number] [x y & rest]
   (reduce * (clojure.core/* x y) rest))
(defmethod * [ENumber nil] [x] x)
(multi-defmethod * [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y & rest]
  (ensure-enumbers [x y]
    (reduce * (e* x y) rest)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti / (fn [& rest]
              (let [[x y] rest]
                [(type x) (type y)])))

(defmethod / [java.lang.Number nil] [x] x)
(defmethod / [java.lang.Number java.lang.Number] [x y & rest]
   (reduce / (clojure.core// x y) rest))
(defmethod / [ENumber nil] [x] x)
(multi-defmethod / [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y & rest]
  (ensure-enumbers [x y]
    (reduce / (e-div x y) rest)))
