# Using transactions: execute() semantics and plugin wiring

Surprising behavior of the transaction API surface and how to layer scoped transactions.

- **`RaptorTransaction.execute()` runs a NEW child transaction, not the receiver.**
  `execute(block)` ignores the receiver's own lifecycle: it calls `context.transaction()` to
  create a fresh transaction, then `start()`/`stop()`s *that* one and runs `block` in its
  context. Combined with `RaptorScope.transaction { }` (= `context.transaction().execute(block)`),
  the scope helper actually creates **two** transactions — T1 from the factory, then T2 as a
  child of T1's context — and only T2 is started/stopped; T1 is created but never run. Anchors:
  `modules/transactions/sources-jvm/api/RaptorTransaction.kt` (`execute`),
  `modules/transactions/sources-jvm/api/RaptorScope.kt` (`transaction`).
- **The transactions plugin must be installed at the assembly ROOT.** Both the
  `RaptorAssemblyQuery<RaptorTransactionBoundary<*>>.transactions` extension and
  `transactionFactory()` first do `componentRegistry.root.oneOrNull(Keys.transactionsComponent)`
  and throw `RaptorPluginNotInstalledException(RaptorTransactionPlugin)` if absent from the
  **root** registry — before registering/reading on the local registry. The root check is a
  discarded expression used purely for its throwing side effect (easy to misread as redundant).
  Installing only on a nested component is insufficient. Anchor:
  `modules/transactions/sources-jvm/assembly/RaptorTransactionBoundary.kt` (`transactions`, `transactionFactory`).
- **`transactionFactory()` silently falls back to `RaptorTransactionFactory.empty`** when the
  root has the component but the *local* registry does not — a no-op factory (no configurations,
  no observers) rather than an error, which can mask missing local boundary wiring.
- **Nested transactions inherit parent properties unless re-registered.** The intended pattern
  is to inspect `parentContext[propertyKey]` inside `onCreate` and decide what to register; a
  child that registers nothing keeps the parent's value. `parentContext` can be `is
  RaptorTransactionContext`-checked to distinguish a root context from a transaction parent.

**Scoped-transaction recipe (from test utilities):** a component implements
`RaptorTransactionBoundary<Self>` and captures `transactionFactory()` into a field in
`onConfigurationEnded`; its plugin's `complete()` publishes that factory under a property key;
a domain extension reads it and calls `createTransaction(context = this) {
propertyRegistry.register(key, scopedState) }`.
</content>
