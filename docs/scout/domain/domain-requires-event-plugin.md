# RaptorDomainPlugin unconditionally needs RaptorEventPlugin installed

Any `raptor {}` assembly that installs `RaptorDomainPlugin` and starts its lifecycle must also
`install(RaptorEventPlugin)` — this is not obvious from `RaptorDomainPlugin` itself and fails only at
runtime, deep in DI resolution.

`RaptorAggregatesComponent.completeIn` (`modules/domain/sources/assembly/RaptorAggregatesComponent.kt`)
constructs `DefaultAggregateManager` with `eventEmitter = get()` and `eventSource = get()` unconditionally
— every aggregate event and projection event is emitted through `RaptorEventEmitter`
(`DefaultAggregateManager`'s `process`/`start`), regardless of whether any `RaptorDomainStreamHook` is
registered. Only `RaptorEventPlugin` provides `RaptorEventEmitter`/`RaptorEventSource` into DI (see
`event/event-module-status.md`), and only when `RaptorDIPlugin` is also present. Omitting
`install(RaptorEventPlugin)` compiles fine and fails only when the aggregate manager starts, with
`error("Cannot resolve dependency for key 'io.fluidsonic.raptor.event.RaptorEventSource'.")` (or
`RaptorEventEmitter`) thrown from `DefaultRaptorDI` — nothing in the message points back to the domain
module.

Every domain test module (`EventTests.kt`, `ExecutionTests.kt`, `HookFilteringTests.kt`) installs
`RaptorDIPlugin`, `RaptorDomainPlugin`, `RaptorEventPlugin`, and `RaptorLifecyclePlugin` together for
this reason — copy that set, not just the two that seem relevant to what you're testing.
