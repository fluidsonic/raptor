# Component model: self-query, hidden back-refs, ownership gating

Non-obvious invariants of `RaptorComponent` and its registration.

- **A component *is* a single-element query.** `RaptorComponent` extends
  `RaptorAssemblyQuery<Component>` and implements `each { }` by casting `this as Component`
  and applying the lambda to itself. The query combinators (`filter`/`map`/`invoke`) all
  compose on this self-emitting behavior. Anchors: `RaptorComponent.kt`,
  `RaptorAssemblyQuery.kt`.
- **`Component.registration` is not a field** — it is an extension stored in the
  component's extension map under `Keys.registrationComponentExtension`, written in
  `RaptorComponentRegistration`'s init (`RaptorComponentRegistration.kt`, the
  `Component.registration` extension property). Reading it before the component is
  registered throws `error("Component hasn't been registered.")`. `componentRegistry` is
  *derived, not stored*: `RaptorComponentRegistry.kt` (the `RaptorComponent<*>.componentRegistry`
  extension) is `get() = registration.registry`, so it inherits the same not-registered
  failure. `DefaultComponentExtensionSet.toString()` filters both reserved keys
  (`registrationComponentExtension`, `registryComponentExtension`) out, so the framework
  stashes its own bookkeeping in the same extension bag users write to. `registryComponentExtension`
  (`Keys.kt`) is never actually written — only that toString filter references it.
- **`onConfigurationEnded` is gated by plugin ownership.**
  `RaptorComponentRegistration.complete()` silently returns when `component.plugin != plugin`,
  so the end-callback fires exactly once and only when the *owning* plugin (active during
  registration) completes — not when some other plugin completes.
- **Some subsystems multiplex all components under one erased key.** e.g.
  `modules/domain/.../assembly/Keys.kt` defines a single `RaptorComponentKey("aggregate")`
  for every aggregate component regardless of type; `aggregateComponentOf<>()` unchecked-casts
  it. `RaptorComponentKey` uses **identity equality**: its base `RaptorKey` (`RaptorKey.kt`)
  defines `equals` as `this === other` with an identity `hashCode`, so the `label` string
  is cosmetic (`toString`/error messages only). `DefaultComponentRegistry.setsByKey` is a
  map keyed by key identity, so two keys sharing a label are distinct and never collide —
  e.g. three separate `RaptorComponentKey("entities")` exist across the entities module
  (its bson, configuration, and graphql components).

Test fixtures encode the convention: a `RaptorComponent.Base` subclass passes its owning
plugin to `super`, exposes results by `propertyRegistry.register(...)` in
`onConfigurationEnded`, and the plugin installs the component in `install`.
