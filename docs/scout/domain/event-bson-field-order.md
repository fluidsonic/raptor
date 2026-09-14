# Aggregate event BSON decode has a hard field-order contract

`RaptorAggregateEventBson`'s decoder is stateful and order-dependent: `aggregateType` must be
read before `aggregateId` and `changeType`; `changeType` must be read before `change`. This
is because each field's decode resolves the *next* field's schema (the discriminator resolves
which `RaptorAggregateDefinition`/change definition applies), so an out-of-order document
can't be decoded at all. Anchor: `modules/domain-mongo/sources/assembly/RaptorAggregateEventBson.kt`.

Violating the order throws "Invalid field order when decoding ..." (raised by `checkNotNull`
on the not-yet-resolved `definition`/`changeDefinition`). A manual database edit or a reordered
encoder that changes field order therefore fails loudly at decode time — it does not decode
into wrong data silently.

Related: `event-store-contract.md`, `aggregate-loader-interface.md`.
