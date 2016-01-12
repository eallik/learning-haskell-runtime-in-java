public class LazyEvaluator {
  public static void main(String[] args) {
    IO.putStrLn(Thunk.ready("let's demonstrate generating, mapping and filtering of lists:"));
    Thunk<LList<Integer>> nums = LList.generate(Thunk.ready(0), incrByI(Thunk.ready(1)));
    Thunk<LList<Integer>> dblNums = Fn.apply2(LList.map(), mulByI(Thunk.ready(2)), nums);

    Thunk<Fn<Integer, Boolean>> isEven = Thunk.ready(x -> even(x));
    Thunk<LList<Integer>> evens = Fn.apply2(LList.filter(), isEven, nums);
    IO.print(Fn.apply2(LList.take(), Thunk.ready(3), evens));

    IO.putStrLn(Thunk.ready("let's demonstrate generating an infinite sequence of primes"));
    Thunk<LList<Integer>> primes_ = primes();
    Thunk<LList<Integer>> doublePrimes = Fn.apply2(LList.map(), mulByI(Thunk.ready(2)), primes_);
    IO.print(Fn.apply2(LList.take(), Thunk.ready(10), doublePrimes));

    /////////////////////
    IO.putStrLn(Thunk.ready("let's demonstrate the if function:"));

    Thunk<Integer> value =
      If.if_(Thunk.ready(true), Thunk.ready(3), Thunk.ready(4));
    IO.print(value);

    /////////////////////
    IO.putStrLn(Thunk.ready("let's build an inifinite, but boring list of lists:"));

    Thunk<LList<LList<Integer>>> listOfList =
      LList.generate(LList.nil(),
                     prepend(Thunk.ready(1)));

    IO.putStrLn(LList.pretty(Fn.apply2(LList.take(),
                                       Thunk.ready(10),
                                       listOfList)));

    ////////////////////
    IO.putStrLn(Thunk.ready("let's build another inifinite, slightly less boring list of lists:"));
    // [], [0], [0,1], [0,1,2], [0,1,2,3], ...

    Thunk<Fn<Integer, LList<Integer>>> takeNNums =
      Thunk.ready(x -> Fn.apply2(LList.take(), x, nums));

    Thunk<LList<LList<Integer>>> numsPrefixes =
      Fn.apply2(LList.map(), takeNNums, nums);

    IO.print(Fn.apply2(LList.take(), Thunk.ready(10), numsPrefixes));

    /////////////////////
    IO.putStr  (Thunk.ready("let's build another inifinite list, but this "));
    IO.putStrLn(Thunk.ready("time each of its elements is in turn another infinite list:"));
    // [0,1,2...], [1,2,3...], [2,3,4...], [3,4,5...], ...

    Thunk<Fn<Integer, LList<Integer>>> incrNumsBy =
      Thunk.ready(x -> Fn.apply2(LList.map(), incrByI(x), nums));

    Thunk<LList<LList<Integer>>> numsIncremented =
      Fn.apply2(LList.map(), incrNumsBy, nums);

    // let's take 10 of each infinite nested list, and then 10 of the top-level list.
    IO.print(Fn.apply2(LList.take(),
                       Thunk.ready(10),
                       Fn.apply2(LList.map(),
                                Fn.apply(LList.take(), Thunk.ready(10)),
                                numsIncremented)));
  };

  static <A> Thunk<Fn<LList<A>, LList<A>>> prepend(final Thunk<A> x) { return Thunk.ready(
     xs -> LList.cons(x, xs)
  ); }

  static Thunk<Fn<Double, Double>> incrByD(final Thunk<Double> d) { return Thunk.ready(arg -> Thunk.lazy(__ -> arg.eval() + d.eval())); }
  static Thunk<Fn<Double, Double>> mulByD(final Thunk<Double> factor) { return Thunk.ready(arg -> Thunk.lazy(__ -> arg.eval() * factor.eval())); }
  static Thunk<Fn<Integer, Integer>> incrByI(final Thunk<Integer> d) { return Thunk.ready(arg -> Thunk.lazy(__ -> arg.eval() + d.eval())); }
  static Thunk<Fn<Integer, Integer>> mulByI(final Thunk<Integer> factor) { return Thunk.ready(arg -> Thunk.lazy(__ -> arg.eval() * factor.eval())); }

  static Thunk<Boolean> even(final Thunk<Integer> i) {
    return eq(mod(i, Thunk.ready(2)), Thunk.ready(0));
  }

  static Thunk<Boolean> eq(final Thunk<Integer> a, final Thunk<Integer> b) { return Thunk.lazy(__ -> {
    return a.eval() == b.eval();
  }); }

  static Thunk<Double> sum(final Thunk<LList<Double>> xs) {
    return If.if_(LList.isNil(xs),
                  Thunk.ready(0.0),
                  Thunk.lazy(__ -> LList.head(xs).eval() + sum(LList.tail(xs)).eval()));
  }

  static Thunk<Double> avg(final Thunk<LList<Double>> xs) { return Thunk.lazy(__ ->  {
    final Integer n = LList.len(xs).eval();
    return n == 0
      ? 0.0
      : sum(xs).eval() / n;
  }); }

  static Thunk<Integer> mod(final Thunk<Integer> dividend, final Thunk<Integer> divisor) { return Thunk.lazy(__ -> {
    return dividend.eval() % divisor.eval();
  }); }

  static Thunk<Boolean> gt(final Thunk<Integer> a, final Thunk<Integer> b) { return Thunk.lazy(__ -> {
    return a.eval() > b.eval();
  }); }

  static Thunk<LList<Integer>> primes() {
    return sieve(LList.generate(Thunk.ready(2), incrByI(Thunk.ready(1))));
  }
  static Thunk<LList<Integer>> sieve(Thunk<LList<Integer>> xs_) { return Thunk.lazy(__ -> {
    final Thunk<Integer>  p  = LList.head(xs_);
    Thunk<LList<Integer>> xs = LList.tail(xs_);

    Thunk<Fn<Integer, Boolean>> pred = Thunk.ready(x -> gt(mod(x, p), Thunk.ready(0)));

    return LList.cons(p, sieve(Fn.apply2(LList.filter(), pred, xs))).eval();
  }); }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
