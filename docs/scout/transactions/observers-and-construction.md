# Transaction observers, construction, and lazy context

Internals of `DefaultTransaction` and its builder.

- **Observers run forward on start, reverse (LIFO) on stop/fail, with partial rollback.**
  `DefaultTransaction` calls `onStart` in forward order (0..n-1) and `onStop`/`onFail` in
  reverse. If an observer's `onStart` or `onStop` throws, already-processed observers are
  rolled back via `onFail` (the failing observer itself is NOT given `onFail`), state → failed,
  error rethrown. On a stop failure only the not-yet-stopped observers (index-1..0) get
  `onFail` — observers that already stopped successfully are not rolled back. Anchor:
  `modules/transactions/sources-jvm/DefaultTransaction.kt` (`start`, `stop`).
- **`fail()` never throws, unlike `start()`/`stop()`.** `fail()` iterates observers in reverse
  calling `onFail`, catches every observer exception and attaches it as suppressed on the
  caller's error, then returns normally (state → failed). All three throw on incompatible
  state (e.g. fail/stop before start). Anchor: `DefaultTransaction.kt` (`fail`).
- **Transaction↔context circular construction via lazy placeholders.**
  `DefaultTransactionBuilder.build()` must produce a context that carries the transaction
  itself in its property set (under `Keys.transactionProperty`), while the builder's separate
  `lazyContext` field is resolved to point back at that context. It registers a
  `LazyTransaction` placeholder into the property registry *before*
  the `DefaultTransaction` exists, builds the context, `lazyContext.resolve(context)`,
  constructs the transaction, then `lazyTransaction.resolve(transaction)`. Anchor:
  `DefaultTransactionBuilder.kt` (`build`).
- **Lazy transaction/context throw until configuration completes.** `LazyTransaction` /
  `LazyTransactionContext` start with a null delegate; every member calls `requireDelegate()`
  which errors "cannot be used until ... configuration has completed." `resolve()` is
  single-shot (`check(delegate == null)`). `toString()` is the *only* member safe pre-resolution
  (returns a placeholder). `RaptorTransactionConfigurationScope.lazyContext` exposes this as if
  usable during config, but any access other than `toString` throws.
- **Static configurations apply before the per-call configuration**
  (`DefaultTransactionFactory.createTransaction`), so a per-call block can override static ones.

**Copy-paste bug in `RaptorTransactionsComponent.Observe`:** `onStart()`, `onStop()`, and
`onFail()` all `check(failAction == null)` — `onStart`/`onStop` never guard their own
`startAction`/`stopAction`. So you can register two `onStart` actions with no error (last
wins), but registering an `onStart`/`onStop` *after* an `onFail` throws the wrong "Cannot
define multiple start actions." error.
</content>
