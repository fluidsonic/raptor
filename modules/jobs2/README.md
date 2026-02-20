# raptor-jobs2

Type-safe, event-sourced job system for Raptor. A job is like a GitHub Actions workflow run: **stable identity with multiple execution attempts**. The last execution determines the outcome.


## Core Concepts

| Concept | Type | Description |
|---------|------|-------------|
| **Job** | `RaptorJob<Input, Output>` | A single logical work unit with stable identity |
| **Execution** | `RaptorJobExecution<Output>` | One attempt to run a job; a job may have many |
| **Description** | `RaptorJobDescription<Input, Output>` | Declarative type descriptor with serializers |
| **Command** | `RaptorJobCommand<Input, Output>` | Intent to mutate job state (imperative verbs) |
| **Change** | `RaptorJobChange<Input, Output>` | Recorded fact of what happened (past tense) |
| **Status** | `RaptorJobStatus<Output>` | High-level derived status |


## Job Lifecycle

```
Pending ──→ Running ──→ Succeeded
   │           │
   │           ├──→ Failed
   │           │
   │           └──→ Canceling ──→ Canceled
   │
   └──→ Canceled (before any execution)
```

- **Pending** — created, no execution started yet
- **Running** — last execution is in progress
- **Succeeded** — last execution completed successfully (wins even if cancellation was requested)
- **Failed** — last execution failed; carries optional `message` and `retryable` flag
- **Canceling** — cancellation requested while an execution is running
- **Canceled** — cancellation completed (no running execution)


## Execution Lifecycle

```
Started ──→ Succeeded
   │
   ├──→ Failed
   │
   └──→ Canceled
```


## Retry Model

Each retry creates a **new execution** within the same job — like re-running a GitHub Actions workflow. The job identity stays stable; only the execution list grows. The last execution's outcome determines the job status.


## ID Types

All IDs use phantom type parameters for compile-time safety:

- `RaptorJobId<Input, Output>` — identifies a job instance, implements `RaptorAggregateId`
- `JobDescriptionId<Input, Output>` — identifies a job type
- `JobExecutionId` — identifies a specific execution attempt

The `<Input, Output>` parameters are phantom types — they carry no runtime data but prevent mixing IDs across incompatible job types at compile time.


## Design

- **Event-sourced** — reuses Raptor domain aggregate infrastructure (`RaptorAggregateChange`, `RaptorAggregateId`)
- **Command/Change duality** — commands express intent (`StartExecution`), changes record facts (`ExecutionStarted`)
- **Last-execution-wins** — job status is derived from the last execution's status plus cancellation state
- **Failure info** — `Failed` states carry optional `message: String?` and `retryable: Boolean` (defaults: `null`, `true`)


## Current Limitations

- **No aggregate implementation** — `RaptorJob` is the projection; no `JobAggregate` with `execute()`/`handle()` yet
- **No execution engine** — no scheduler, no execution loop
- **No persistence** — no storage backend
- **No plugin** — no `RaptorJobs2Plugin`, no DI wiring
- **Service2 stubs** — `RaptorServiceJobExtensions2.kt` has `TODO()` placeholders


## Dependencies

- `raptor-core`
- `raptor-domain`
- `fluid-time`
- `kotlinx-coroutines-core`
- `kotlinx-serialization-core`
