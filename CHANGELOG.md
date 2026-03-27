# Changelog

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
