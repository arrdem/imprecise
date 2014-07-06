# Imprecise

> 2 + 2 = 5 is in fact true for sufficiently large values of 2
>
> – DeWayne E. Perry

Imprecise is a library which provides imprecise (toleranced) arithmetic
operations for Clojure by defining new and extensible +, -, * and /
functions.

## Usage

```clojure
user> (require '[imprecise.core :as imp]
               '[clojure.algo.generic.arithmetic :refer :all])
nil
user> (+ 1 1 1)
3
user> (imp/e 1 0.1)
#me.arrdem.imprecise.ENumber{:val 1, :tol 0.1}
user> (+ 1 0.1 (imp/e 1 0.1))
#me.arrdem.imprecise.ENumber{:val 2.1, :tol 0.1}
user> (* 1 0.1 (imp/e 1 0.1))
#me.arrdem.imprecise.ENumber{:val 0.1, :tol 0.010000000000000002}
user> (/ 50 (imp/e 10 0.1))
#me.arrdem.imprecise.ENumber{:val 5, :tol 5.0}
```

## Warning

Be warned that this library was hacked in an evening by a college student and
that neither warranty nor proof of correctness is provided for this software.

Please do verify the code before you use it in something critical, and please
submit a pull request if you find errors.

## License

Copyright © 2014 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
