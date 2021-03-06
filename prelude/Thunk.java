package prelude;

/** This *is* the lazy evaluator a.k.a. graph reducer.
 *
 * Entire Haskell programs are represented as graphs of Thunks.
 * Since a Thunk can represent a computation, which can in turn create more of the graph,
 * conceptually, graphs can be infinite, because these computations are run transparently 
 * and automatically. It is therefore possible to define a graph without thinking about
 * how large it is, as only a little bit of it will ever be represented in memory at once.
 */
public final class Thunk<A> {
  /** Returns the value this Thunk wraps. */
  public A eval() {
    if (value != null) {
      return value;
    } else {
      value = computation.compute(null);
      return value;
    }
  }

  /** Make a Thunk with an already computed value in it. */
  public static <A> Thunk<A> ready(            A  x) { return new Thunk<A>(x); }
  /** Make a Thunk with a lazily executed computation in it. */
  public static <A> Thunk<A> lazy (Computation<A> x) { return new Thunk<A>(x); }

  public static <A, B> Thunk<Fn<A, B>> fn   (Fn<A, B>       x) { return ready(x); }
  public static <A>    Thunk<A>        thunk(            A  x) { return ready(x); }
  public static <A>    Thunk<A>        thunk(Computation<A> x) { return lazy (x); }

  ///

  private                   A  value = null;
  private final Computation<A> computation;

  // we hide the constructors, because want to make the difference in their
  // meanings explicit via the methods `ready` and `lazy` above.

  private Thunk(Computation<A> computation) {
    this.computation = computation;
  }
  private Thunk(A value)            {
    // FP *never* uses null pointers as real values, so we can safely use
    // nulls inside our low-level, (hopefully) hidden plumbing.
    assert(value != null);

    this.value = value;
    this.computation = null;
  }

  /** a helper interface for Thunk.lazy that should not be used directly */
  public static interface Computation<A> { A compute(Void v); }
}
