(ns imprecise
  {:author "Reid McKenzie"
   :doc "Math with units
         This library implements algo.generic math operations
         across values which have attached symbolic units. This
         library makes no attempt to provide transition functions
         or automatic conversion, it simply provides unit tracking
         through algebraic operations."}
  (:require [clojure.algo.generic
             [arithmetic :refer :all]
             [comparison :as c]
             [math-functions :refer :all]])
  (:refer-clojure :exclude [- + / *]))


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
    (tol-min   [self] (- (. self val) (. self tol)))
    (tol-max   [self] (+ (. self val) (. self tol)))

  (toString  [self] (str (.val self) "±" (.tol self))))

(defn e [x y] (ENumber. x y))

(defn e+ [x y]
  (ENumber. 
   (+ (to-scalar x)
      (to-scalar y))
   (+ (tolerance x)
      (tolerance y))))

(defn e- [x y]
  (ENumber.
   (- (to-scalar x)
      (to-scalar y))
   (+ (tolerance x)
      (tolerance y))))

(defn e* [x y]
  (ENumber. 
   (* (to-scalar x)
      (to-scalar y))
   (+ (* (to-scalar x)
         (tolerance y))
      (* (to-scalar y)
         (tolerance x)))))

(defn e-div [x y]
 (ENumber.
  (/ (to-scalar x)
     (to-scalar y))
  (/ (- (* (tolerance x) 
           (+ (to-scalar y)
              (tolerance y)))
        (* (tolerance y)
           (+ (to-scalar x)
              (tolerance x))))
     (+ (* (to-scalar y)
           (to-scalar y))
        (* 2
           (to-scalar y)
           (tolerance y))
        (* (tolerance y)
           (tolerance y))))))

(defmacro multi-extend-type 
  [types & rest]
  `(do ~@(for [t types]
           `(extend-type ~t ~@rest))))

(multi-extend-type [java.lang.Number
                    java.math.BigDecimal
                    java.math.BigInteger
                    java.lang.Byte
                    java.lang.Long
                    java.lang.Integer
                    java.lang.Double
                    java.lang.Float
                    java.lang.Short]
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
(multi-defmethod + [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y & rest]
  (ensure-enumbers [x y]
    (e+ x y)))


(multi-defmethod - [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y & rest]
  (ensure-enumbers [x y]
    (e- x y)))


(multi-defmethod * [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y]
  (ensure-enumbers [x y]
    (e* x y)))


(multi-defmethod / [[java.lang.Number ENumber]
                    [ENumber java.lang.Number]
                    [ENumber ENumber]]
                 [x y]
  (ensure-enumbers [x y]
    (e-div x y)))
