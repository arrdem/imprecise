(ns imprecise.core-test
  (:refer-clojure :exclude [- + / * min max])
  (:require [clojure.test :refer :all]
            [clojure.algo.generic
             [arithmetic :refer [+ - / *]]
             [comparison :as c]
             [math-functions :refer :all]]
            [imprecise.core :refer :all]))


(deftest arithmetic-tests
  (let [x (->AInterval 0 5)
        y (->AInterval 0 3)]
    (is (c/= (->AInterval 0 8)  (+ x y)))
    (is (c/= (->AInterval 3 5)  (- x y)))))


(deftest multiplicative-tests
  (let [x (->AInterval 0 16)
        y (->AInterval 2 2)]
    (is (c/= (->AInterval 0 8)  (/ x y)))
    (is (c/= (->AInterval 0 32) (* x y)))))


(deftest comparison-tests
  (testing "Testing c/<"
    (is (c/< (->AInterval 0 0)      (->AInterval 1 1)))
    (is (c/< (->AInterval 0 0)      (->AInterval 1 Double/MAX_VALUE)))
    (is (not (c/< (->AInterval 0 0) (->AInterval 0 1)))))

  (testing "Testing c/<="
    (is (c/<= (->AInterval 0 0) (->AInterval 0 1)))
    (is (c/<= (->AInterval 0 0) (->AInterval 0 Double/MAX_VALUE)))
    (is (c/<= (->AInterval 0 0) (->AInterval 1 1)))
    (is (c/<= (->AInterval 0 0) (->AInterval 1 Double/MAX_VALUE))))

  (testing "Testing c/>"
    (is (c/> (->AInterval 1 1)                (->AInterval 0 0)))
    (is (c/> (->AInterval 1 Double/MAX_VALUE) (->AInterval 0 0)))
    (is (c/> (->AInterval 1 1)                (->AInterval Double/MIN_VALUE 0)))
    (is (not (c/> (->AInterval 0 1)           (->AInterval 0 0)))))

  (testing "Testing c/>=")
    (is (c/>= (->AInterval 0 1)                (->AInterval 0 0)))
    (is (c/>= (->AInterval 0 Double/MAX_VALUE) (->AInterval 0 0)))
    (is (c/>= (->AInterval 1 1)                (->AInterval 0 0)))
    (is (c/>= (->AInterval 1 Double/MAX_VALUE) (->AInterval 0 0))))
