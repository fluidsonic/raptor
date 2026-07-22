# DI resolution: last-wins override, nullable-optional, memoized locking

How `RaptorDI` resolves and overrides dependencies. Two of these are emergent from
combined design choices in different files, with no "last wins" comment anywhere.

- **Override = last provider wins**, via two cooperating mechanisms:
  (1) `RaptorDI.Module.providerForKey` selects `providers.lastOrNull { it.key == key }`
  (last registered in a module wins); (2) `DefaultRaptorDI` stores `modules.reversed()` and
  resolves in that reversed order, so later-appended modules are consulted first. In
  `Factory.createDI` the order is `modules + inlineModule + contextModule`, so
  inline/context providers override base modules. Anchors: `di/RaptorDI.kt` (`providerForKey`),
  `di/DefaultRaptorDI.kt`.
- **Optionality lives on the key/consumer side, keyed off nullability.**
  `isOptional = type.isMarkedNullable` (`KTypeDIKey.kt`). `get(key)` returns `null` for an
  unresolved optional key but throws `reportMissingDependency` for a non-optional one. So
  `di.get<Foo?>()` ≡ `di.getOrNull<Foo>()`. `provide` and `provideOptional` funnel to the
  same builder call — there is no distinct optional provider path.
- **Resolution is lazy, memoized, cycle-checked, under one lock.** Every `getOrNull`
  acquires a per-instance `ReentrantLock` and memoizes results *including nulls* via
  `getOrPutNullable` (a custom helper in `utility/Map.x.kt`, needed because plain `getOrPut`
  can't distinguish "cached null" from "absent"). Provider lambdas run at most once per key.
  Cycles are caught with a `currentlyResolvingKeys` stack. A TODO notes there is no
  fast-path — even cached reads take the lock.

`RaptorDIKey(type)` rebuilds the type with `type.withNullability(type.isMarkedNullable)`
(comment "Remove platform type.") to normalize Java-interop platform types so key equality
(`type == other.type`) is stable.

Related: `di/builder-and-scopes.md`.
</content>
