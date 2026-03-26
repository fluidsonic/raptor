# Changelog

## [Unreleased]

### Added
- Comprehensive tests for `store`, `store-memory`, and `store-mongo` modules covering all code paths
- In-memory mock MongoDB infrastructure (`TestMongoCollection`, `TestMongoDatabase`, `TestFindFlow`) for testing store-mongo without a database

### Changed
- Renamed `key-value-store` modules to `store`, `store-memory`, and `store-mongo`.
- Changed package names from `io.fluidsonic.raptor.keyvaluestore` to `io.fluidsonic.raptor.store`.

### Removed

- `quickstart` module

## [0.32.0] - 2026-03-10

### Added

- `RaptorDomain` facade for unified access to domain streams and operations
- `RaptorDomainStreamHook` interface for hooking into domain stream lifecycle

### Changed

- Bulk replay is now emitted as a single `Replay` message instead of individual messages, improving replay consistency
- Cold-replay pipeline decoupled from subscriber processing — event replay and stream subscriber handling are now independent
- Projection loader is gated during replay to prevent premature loading
- Increased stream buffer size for better throughput during replay
