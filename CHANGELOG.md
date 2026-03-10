# Changelog

## [Unreleased]

## [0.32.0] - 2026-03-10

### Added

- `RaptorDomain` facade for unified access to domain streams and operations
- `RaptorDomainStreamHook` interface for hooking into domain stream lifecycle

### Changed

- Bulk replay is now emitted as a single `Replay` message instead of individual messages, improving replay consistency
- Cold-replay pipeline decoupled from subscriber processing — event replay and stream subscriber handling are now independent
- Projection loader is gated during replay to prevent premature loading
- Increased stream buffer size for better throughput during replay
