# Changelog

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
