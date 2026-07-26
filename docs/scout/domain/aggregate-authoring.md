# Authoring aggregates and projectors (framework contracts)

Conventions a `RaptorAggregate`/projector implementation must follow, demonstrated by the
`BankAccount` test fixtures. These are silent footguns — the compiler won't catch them.

- **`copy()` must be hand-written and replicate every mutable field.** Aggregates hold
  mutable in-place state (`var amount`/`isCreated`/…) and override `copy()` to manually
  reconstruct a new instance copying each field — this is the snapshot/rollback mechanism
  (see `domain/command-execution.md`, where commit mutates a defensive copy). Omitting a
  field silently loses state on copy with no compiler error. Anchor:
  `modules/domain/tests/domain/BankAccountAggregate.kt` (`copy`).
- **Return `null` for a no-op change; `check()` for an invariant violation.** Command
  handlers are wrapped in `listOfNotNull(...)`, so returning `null` emits zero changes.
  "Nothing changed" (deposit of 0, re-labeling to the same label, deleting a not-yet-created
  account) and "illegal" (create-when-created, mutate-before-create, overdraw, delete with
  funds → `check()` throws) are deliberately different outcomes. Anchor:
  `BankAccountAggregate.kt` (`execute`).
- **Sealed-change projectors need an unreachable exhaustiveness branch.**
  `BankAccountProjector.apply` carries `@Suppress("KotlinConstantConditions")` and a
  logically-unreachable `is Created -> error("Compiler error.s")` case (typo included)
  purely to satisfy Kotlin's exhaustiveness checker after the outer `when` already handled
  `Created`. This is the pattern for incremental projectors over a sealed change hierarchy.
  Anchor: `modules/domain/tests/domain/BankAccountProjector.kt` (`apply`).
