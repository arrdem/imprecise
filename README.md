# Imprecise

> 2 + 2 = 5 is in fact true for sufficiently large values of 2
>
> – DeWayne E. Perry

Imprecise is a library which implements intervals and interval
arithmetic for Clojure, and provides some wrappers for working with
intervals to represent imprecise measurements.

[![Clojars Project](http://clojars.org/me.arrdem/imprecise/latest-version.svg)](http://clojars.org/me.arrdem/imprecise)

## Usage

Imprecise is built around a single protocol: `IInterval`

```Clojure
(defprotocol IInterval
  (min   [_] "The minimum value of an Interval.")
  (max   [_] "The maximum value of an Interval.")
  (avg   [_] "The average value of an Interval.")
  (sigma [_] "The standard deviation of an Interval."))
```

Imprecise provides a standard implementation of an `IInterval`, the
concrete class `imprecise.core.AInterval` which can be constructed
from a pair of `Object`s presumed to represent the `(low, high)` pair
denoting a range.

**WARNING** Due to the usage of `min` and `max`, `(require
[... :refer :all])` won't work with Imprecise due to name conflicts
with `clojure.core/min` and `clojure.core/max`

```clojure
user> (require '[imprecise.core :as imp])
nil

user> (imp/->AInterval 1 5)
{x|x∈[1 ... 5]}
```

The helper function `e` is provided, which serves to provide two more
cases for constructing values:

```Clojure
user> (doc imp/e)
-------------------------
imprecise.core/e
([base err] [base -err +err])
  (λ Base → Err) → IInterval
  (λ Base → +Err → -Err) → IInterval

  In the two argument case, returns an Interval representing the
  rage [Base-Err, Base+Err].

  In the three argument case, returns an Interval representing the range
  [Base + -Err, Base + +Err].

user> (imp/e 1 0.1)
{x|x∈[0.9 ... 1.1]}
```

The bulk of Imprecise defines how Intervals behave with addition,
subtraction, multiplication and division.

```Clojure
user> (require '[clojure.algo.generic.arithmetic :refer :all])
nil

user> (+ 1 0.1 (imp/e 1 0.1))
{x|x∈[2.0 ... 2.2]}

user> (* 1 0.1 (imp/e 1 0.1))
{x|x∈[0.09000000000000001 ... 0.11000000000000001]}

user> (/ 50 (imp/e 10 0.1))
{x|x∈[4.9504950495049505 ... 5.05050505050505]}
```

All arithmetic on Intervals is defined in terms of
`clojure.algo.generic` operations and thus is extensible to custom
numeric types. Note however that due to the user provided extension
properties of Clojure multimethods Intervals don't provide
implementations of arithmetic with anything other than
`java.lang.Number` and children.

Imprecise also defines `algo.generic` comparisons over intervals. I
confess that I don't find these operations entirely intuitive and
comment is welcome on their correctness.

```Clojure
user> (require '[clojure.algo.generic.comparison :refer :all])
nil

user> (< (imp/e 0 1) (imp/e 1.1 2))
true

user> (< (imp/e 0 1) (imp/e 1 2))
false

user> (<= (imp/e 0 1) (imp/e 1 2))
true
```

## Warning

Be warned that this library was hacked in an evening by a college student and
that neither warranty nor proof of correctness is provided for this software.

Please do verify the code before you use it in something critical, and please
submit a pull request if you find errors.

## License

Copyright © 2014 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
