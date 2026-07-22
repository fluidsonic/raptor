# Registry internals: write-once, disabled guards, re-entrant loops

Traps inside the core registries used during assembly.

- **Write-once keys.** `DefaultKeyValueRegistry.register` and
  `DefaultComponentExtensionSet.set` use `MutableMap.putIfAbsent` and treat any non-null
  return as a hard `error()` (printing both existing and new values). Keys are
  single-assignment; there is no overwrite path.
- **Configuration-phase-ended guards are entirely stubbed out.**
  `DefaultComponentRegistry.checkIsConfigurable` is a no-op — its body and the backing
  `configurationEnded` flag are commented out (TODO "Needs a per-plugin check"). Every
  error message like "Cannot register a component after the configuration phase has ended"
  is dead: it cannot currently fire. Do not trust that lifecycle guard to protect
  ordering.
- **Registration lists are iterated by index on purpose.**
  `DefaultComponentRegistry.RegistrationSet` `add()`/`each()` use `for (index in indices)`
  (not for-each) because invoking a configuration may append more configurations/components
  mid-iteration; anything added during iteration is applied immediately. Rewriting these as
  idiomatic `forEach` would throw `ConcurrentModificationException` or double-apply. `each()`
  also skips registrations whose `configurationStarted` flag is still false.
- **`complete()` recurses children before parents.**
  `DefaultComponentRegistry.complete()` runs two passes: first `registration.registry.complete(...)`
  on each child registry (depth-first), then `registration.complete(...)` on the
  registrations. Descendant components complete before their parent registration.
- **The root context throws until assembly finishes.** `LazyRootContext` (a
  `RaptorContext.Lazy`) has a null delegate; the `context` and `properties` accessors call
  `requireDelegate()` (only `parent` short-circuits to null), which
  throws "This context cannot be used until the configuration of all components and plugins
  has completed." `resolve()` is called once at the end of `Completion.init`. Plugins get
  this lazy context during completion — dereferencing it too early fails deliberately.

Related: `assembly/raptor-key-identity.md`, `assembly/plugin-lifecycle.md`.
</content>
