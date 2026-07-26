# Changelog

## [0.38.0] - 2026-07-26

### Added
- `RaptorGraphResolverScope`, the scope a field's or operation's `resolver { … }` now runs in — it carries the surrounding transaction, and with it dependency injection, exactly as the output scope did before

### Changed
- Updated fluid-graphql to 0.17.0
- GraphQL parsing and serialization no longer have access to the surrounding transaction: `RaptorGraphInputScope` and `RaptorGraphOutputScope` no longer extend `RaptorTransactionScope`, making a scalar's `parse`/`serialize`, an alias' conversions, an input object's factory and an argument's `map`/`validate` transforms pure transformations of the value they are given. Field resolvers are unaffected — they now run in `RaptorGraphResolverScope`
- Rejecting a value with `invalid(details)` now reaches the client as `The value is invalid: <details>` with extension `code: "invalid value"`, matching how invalid arguments were already reported

### Removed
- `Maybe` support in GraphQL, together with the `@optional` directive — arguments can no longer be declared as `Maybe<T>`
- `GraphId`. It was registered as the `ID` scalar but was never usable: returning one from a field failed output coercion and aborted the request. Declare an ID alias instead, e.g. `definitions.newIdAlias<MyId> { parse { MyId(it) }; serialize(MyId::value) }`
- The deprecated `graphql` module (`io.fluidsonic.raptor:raptor-graphql`), superseded by `raptor-graph`

### Fixed
- Generated schemas no longer declare the built-in scalars `Boolean`, `Float`, `ID`, `Int` and `String`. Raptor registered its own types under those names, which the GraphQL specification forbids — every served schema document contained `scalar Int` and friends, and introspection listed each of the five built-ins twice

## [0.37.0] - 2026-07-22

### Added
- GraphQL queries can now be sent over HTTP `GET` on the Ktor graph route, following the GraphQL-over-HTTP specification: `query`, `operationName`, and `variables` (as a JSON string) are read from the query string. Mutations and subscriptions over `GET` are rejected with `405 Method Not Allowed`

### Fixed
- The Ktor GraphQL route no longer responds with HTTP `500` to requests with an empty or malformed body (such as a `GET`, or invalid/empty JSON). Transport-level problems — invalid JSON, a missing or non-string `query`, a non-string `operationName`, or non-object `variables` — now return HTTP `400` with a GraphQL error body (`{"errors":[{"message":…}]}`) that includes the underlying JSON parse error, while GraphQL parse/validation/execution errors continue to return HTTP `200` with an `errors` array

## [0.36.0] - 2026-06-09

### Added
- Atomic `RaptorKeyValueStore.update` for safe read-modify-write of an entry via an `UpdateDecision` (`Update`/`Keep`/`Remove`) callback. The in-memory store applies the change atomically; backends that use optimistic concurrency retry on conflict and throw the new `RaptorOptimisticUpdateException` once attempts are exhausted

## [0.35.0] - 2026-03-28

### Changed
- Updated Gradle to 9.4.1
- Updated Kotlin to 2.3.20
- Updated `io.fluidsonic.gradle` plugin to 3.0.0
- Updated fluid-country to 0.14.0
- Updated fluid-currency to 0.14.0
- Updated fluid-graphql to 0.16.0
- Updated fluid-i18n to 0.14.0
- Updated fluid-json to 2.0.0
- Updated fluid-locale to 0.14.0
- Updated fluid-mongo to 1.9.0
- Updated fluid-stdlib to 0.15.0
- Updated fluid-time to 0.19.0
- Updated kotlinx-atomicfu to 0.32.1
- Updated kotlinx-serialization to 1.10.0
- Updated Ktor to 3.4.2
- Updated Logback to 1.5.32
- Updated MongoDB driver to 5.6.4
### Removed
- `jobs-quartz-mongo` module

## [0.34.0] - 2026-03-27

### Added
- KDoc for all public API in `store`, `store-memory`, and `store-mongo` modules
- Comprehensive tests for `store`, `store-memory`, and `store-mongo` modules covering all code paths
- In-memory mock MongoDB infrastructure (`TestMongoCollection`, `TestMongoDatabase`, `TestFindFlow`) for testing store-mongo without a database
- Log store abstraction (`RaptorLogStore`, `RaptorLogStoreFactory`) for append-only storage
- In-memory log store implementation with `RaptorLogStoreFactory.memory()`
- MongoDB log store implementation with `RaptorLogStoreFactory.mongo()`

### Changed
- Renamed `key-value-store` modules to `store`, `store-memory`, and `store-mongo`.
- Changed package names from `io.fluidsonic.raptor.keyvaluestore` to `io.fluidsonic.raptor.store`.

### Removed
- `quickstart` module
- Broken `MongoDBTests.testDefaultCodecs` test (hardcoded codec string no longer matches driver)

## [0.32.0] - 2026-03-10

### Added

- `RaptorDomain` facade for unified access to domain streams and operations
- `RaptorDomainStreamHook` interface for hooking into domain stream lifecycle

### Changed

- Bulk replay is now emitted as a single `Replay` message instead of individual messages, improving replay consistency
- Cold-replay pipeline decoupled from subscriber processing — event replay and stream subscriber handling are now independent
- Projection loader is gated during replay to prevent premature loading
- Increased stream buffer size for better throughput during replay
