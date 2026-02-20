# RaptorJobs2 Module Plan

## Context

The `service2` module contains ~475 lines of job system domain model and API surface (`RaptorJob.kt`, `RaptorJobId.kt`, `RaptorServiceJobExtensions2.kt`) with zero runtime wiring. This plan extracts jobs into their own module with a MongoDB persistence implementation.

## Architecture

### Module Structure

- `modules/jobs2/` - Core job abstractions, domain model, and API surface
- `modules/jobs2-mongodb/` - MongoDB persistence implementation

### Core Module (`jobs2`)

Extracted from `service2`:
- `RaptorJob` - Job domain model
- `RaptorJobDescription` - Declarative job type description
- `RaptorJobCommand` / `RaptorJobChange` - Command/change types
- `RaptorJobExecution` / `RaptorJobExecutionStatus` - Execution tracking
- `RaptorJobExecutor` / `RaptorJobExecutionContext` - Job execution interface
- `RaptorJobId` / `JobDescriptionId` / `JobExecutionId` - ID types
- `RaptorJobEnqueuer` / `RaptorJobFetcher` - Read/write interfaces
- `RaptorJobStatus` - High-level status derivation
- `RaptorJobUpdate` - Snapshot + change pair for `follow()` streaming

Plus new runtime wiring:
- `RaptorJobs2Plugin` - Plugin for registering job descriptions and executors
- Job scheduling integration with `RaptorServiceInput2` (replacing the TODO stubs in `ScheduledTaskInputSource`)
- Retry policy configuration on `RaptorJobDescription`

### MongoDB Module (`jobs2-mongodb`)

- MongoDB-backed `RaptorJobEnqueuer` / `RaptorJobFetcher` implementation
- Job document schema with indexes for efficient querying
- Change stream-based `follow()` implementation
- Polling fallback for environments without change streams

## Design Decisions

### Job Domain Model (`RaptorJob`)
- A Job describes a single logical work unit that may have multiple executions (retries).
- The last execution determines the final outcome.
- Lifecycle is currently inferred from `cancellationRequestTimestamp` and last execution status.
- Consider adding explicit status timestamps (`createdAt`, `completedAt`, `canceledAt`) and an explicit `Queued`/`Scheduled` status on executions.

### Error Modeling
- Failures carry `message: String?` and `retryable: Boolean` (defaults: `null`, `true`).
- Present on `RaptorJobExecutionStatus.Failed`, `RaptorJobChange.ExecutionFailed`, and `RaptorJobCommand.FailExecution`.
- Consider expanding to a serializable `FailureReason` (`code`, `message`, `details` map) in the future.

### Retry Policy
- Model on `RaptorJobDescription`: `maxAttempts`, `backoffStrategy`, `retryableFailureCodes`.
- Keep execution policy decisions in the executor/handler.
- Description should be declarative; runtime limits belong in infrastructure.

### Uniqueness & De-duplication
- Some jobs should be unique by input.
- Optional uniqueness key on `RaptorJobDescription` and/or `hash(input)`-based constraints.
- Define behavior on duplicates: ignore, join/follow, enqueue-new.

### Naming
- Commands use imperative verbs: `Create`, `Cancel`, `StartExecution`, `SucceedExecution`, `FailExecution`, `CancelExecution`.
- Changes use past tense: `Created`, `Canceled`, `ExecutionStarted`, `ExecutionSucceeded`, `ExecutionFailed`, `ExecutionCanceled`.
- All public types use `Raptor` prefix: `RaptorJobEnqueuer`, `RaptorJobFetcher`.

### Serialization
- `RaptorJobDescription` exposes serializers for persistence.
- All inputs and outputs must be stable-serializable across versions.
- Consider adding a schema/version field to `JobDescriptionId`.

### ID Types
- Keep type-parameterized IDs for compile-time safety. IDs are opaque and stable.
- `RaptorJobId` implements `RaptorAggregateId` for aggregate infrastructure integration.

### Concurrency & Batching
- Optional hints (`maxConcurrency`, `batchSize`) on description.
- Actual limits belong in infrastructure; description is a hint. Document precedence.

### Invariants
- Executions list is strictly ordered by start time.
- Only the last execution may be running.

### Observability
- Add `correlationId` and `metadata` on Job and Execution for tracing.

### Status Model
- Add `Queued` state to `RaptorJobStatus` to align with execution-level queued state.
- Provide `endedAt` common timestamp on `Ended` to avoid re-deriving.
- Ensure cancellation vs succeeded precedence is clearly defined.

## Status

**Domain model extracted and reviewed.**

### Completed
- Domain types extracted from `service2` into `modules/jobs2/`
- File split: one file per concept (`RaptorJob`, `RaptorJobChange`, `RaptorJobCommand`, `RaptorJobDescription`, `RaptorJobExecution`, `RaptorJobExecutor`, `RaptorJobEnqueuer`, `RaptorJobFetcher`)
- Naming aligned with Raptor DDD conventions (imperative commands, past-tense changes, `Raptor` prefix)
- `RaptorJobEvent` renamed to `RaptorJobChange`
- `RaptorJobId` implements `RaptorAggregateId`
- `RaptorJobUpdate` data class replaces `Pair` in `follow()` return type
- `find()` returns `Flow` instead of `List`
- Failure info (`message`, `retryable`) added to failed types
- `ExecutionCanceled` change added for command/change symmetry
- KDoc added to all public types
- Tests for status derivation, IDs, commands, changes, and update type

### Remaining Work
- **Aggregate class** — `JobAggregate` with `execute()`/`handle()` for event sourcing
- **Plugin** — `RaptorJobs2Plugin` with DI registration
- **Execution engine** — scheduler, execution loop, retry logic
- **Persistence** — `jobs2-mongodb` module
- **Service2 integration** — wire `TODO()` stubs in `RaptorServiceJobExtensions2.kt`
- **Description FIXMEs** — timeout, batch size, max concurrency, unique input, retry
- **Observability** — `correlationId` and `metadata`
