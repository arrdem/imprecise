(ns imprecise.core
  {:doc "Interval arithmetic for Clojure

        This namespace provides an algo.generic based implementation
        of arithmetic operations and comparisons on intervals as well
        as helpers for working with intervals in the special case of
        measurement tolerances."
   :author "Reid McKenzie"}
  (:refer-clojure :exclude [- + / * min max])
  (:require [clojure.algo.generic
             [arithmetic :refer [+ - / *]]
             [comparison :as c]
             [math-functions :refer :all]]))


(defprotocol IInterval
  (min   [_])
  (max   [_])
  (avg   [_])
  (sigma [_]))


(defn- i [F x y]
  (- (F y)
     (F x)))


(deftype AInterval [x₀ x₁]
  IInterval
    (min   [_] x₀)
    (max   [_] x₁)
    (avg   [_] (/ (+ x₀ x₁) 2))
    (sigma [_]
      (let [p (/ 1 (abs (- x₁ x₀)))
            μ (* p (i #(/ (* %1 %1) 2)
                      x₀ x₁))]
        (sqrt
         (* p
            (i #(- (/ (* %1 %1 %1) 3)
                   (* μ %1 %1)
                   (* μ μ %1))
               x₀ x₁)))))

  clojure.lang.IPersistentSet
    (contains [_ x] (and (c/<= x x₁) (c/>= x x₀)))
    (get      [_ x] (if (contains? _ x) x nil))

  java.lang.Object
    (toString [_]
      (format "{x|x∈[%s ... %s]}"
              x₀ x₁)))

(defmethod print-method AInterval [o ^java.io.Writer w]
  (.write w (.toString o)))


(defn e
  ([base err]
     (->AInterval
      (- base err)
      (+ base err)))

  ([base -err +err]
     (->AInterval
      (+ base -err)
      (+ base +err))))


(defn i-add [x y]
  (->AInterval
   (+ (min x) (min y))
   (+ (max x) (max y))))


(defn i-sub [x y]
  (->AInterval
   (- (max x) (min y))
   (- (max y) (min x))))


(defn i-mul [x y]
  (let [x₁y₁ (* (min x) (min y))
        x₁y₂ (* (min x) (max y))
        x₂y₁ (* (max x) (min y))
        x₂y₂ (* (max x) (max y))]
    (->AInterval
     (c/min x₁y₁ x₁y₂ x₂y₁ x₂y₂)
     (c/max x₁y₁ x₁y₂ x₂y₁ x₂y₂))))


(defn i-div [x y]
  {:pre [(not (contains? y 0))]}
  (* x (->AInterval (/ (min y)) (/ (max y)))))


;;--------------------------------------------------------------------

(defmethod + AInterval [x] x)

(defmethod + [AInterval AInterval]
  [x y]
  (i-add x y))

(defmethod + [java.lang.Number AInterval]
  [x y]
  (i-add (e x 0) y))

(defmethod + [AInterval java.lang.Number]
  [x y]
  (i-add x (e y 0)))

;;--------------------------------------------------------------------

(defmethod - AInterval [x] x)

(defmethod - [AInterval AInterval]
  [x y]
  (i-sub x y))

(defmethod - [java.lang.Number AInterval]
  [x y]
  (i-sub (e x 0) y))

(defmethod - [AInterval java.lang.Number]
  [x y]
  (i-sub x (e y 0)))

;;--------------------------------------------------------------------

(defmethod * AInterval [x] x)

(defmethod * [AInterval AInterval]
  [x y]
  (i-mul x y))

(defmethod * [AInterval java.lang.Number]
  [x y]
  (i-mul x (e y 0)))

(defmethod * [java.lang.Number AInterval]
  [x y]
  (i-mul (e x 0) y))

;;--------------------------------------------------------------------

(defmethod / AInterval [x] (/ 1 x))

(defmethod / [AInterval AInterval]
  [x y]
  (i-div x y))

(defmethod / [AInterval java.lang.Number]
  [x y]
  (i-div x (e y 0)))

(defmethod / [java.lang.Number AInterval]
  [x y]
  (i-div (e x 0) y))

;;--------------------------------------------------------------------

(defmethod abs AInterval [x]
  (->AInterval
   (c/min 0 (min x))
   (c/max 0 (max x))))

(defmethod ceil AInterval [x]
  (ceil (max x)))

(defmethod floor AInterval [x]
  (floor (min x)))

(defmethod round AInterval [x]
  (->AInterval
   (round (min x))
   (round (max x))))

(defmethod pow [AInterval java.lang.Number] [x n]
  (->AInterval
   (pow (min x) n)
   (pow (max x) n)))

(defmethod sqrt AInterval [x]
  (->AInterval
   (sqrt (min x))
   (sqrt (max x))))

;;--------------------------------------------------------------------

(defmethod c/pos? AInterval [x]
  (and (c/pos? (min x))
       (c/pos? (max x))))

(defmethod c/neg? AInterval [x]
  (and (c/neg? (min x))
       (contains? x 0)))

(defmethod c/zero? AInterval [x]
  (contains? x 0))

(defmethod c/= [AInterval AInterval] [x y]
  (and (c/= (max x) (max y))
       (c/= (min x) (min y))))

(defmethod c/> [AInterval AInterval] [x y]
  (c/> (min x) (max y)))

(defmethod c/< [AInterval AInterval] [x y]
  (c/< (max x) (min y)))

(defmethod c/>= [AInterval AInterval] [x y]
  (and (contains? y (min x))
       (contains? x (max y))
       (c/>= (max x) (max y))))

(defmethod c/<= [AInterval AInterval] [x y]
  (and (contains? x (min y))
       (contains? y (max x))
       (c/>= (max y) (max x))))
