# Operation naming and the `inputArgumentName` delegate hack

Conventions for defining `RaptorGraphQuery`/`RaptorGraphMutation` operations.

- **Operation name is derived by stripping a class-name suffix.**
  `RaptorGraphOperation.defaultName()` takes `this::class.simpleName`, removes the
  base-class-specific suffix (`RaptorGraphQuery` strips `"Query"`, `RaptorGraphMutation`
  strips `"Mutation"`), and lowercases the first char. So `class CreateUserMutation :
  RaptorGraphMutation<...>` becomes operation `createUser`. Anonymous/local classes (null
  `simpleName`) force an explicit `define(name = ...)`. TODO comments note the guard against
  cross-suffix misuse (a query class named `...Mutation`) is **not** implemented — such a
  class is silently misnamed. Anchor: `modules/graphql/sources-jvm/operations/RaptorGraphOperation.kt` (`defaultName`).
- **Default type names.** `RaptorGraphOperationBuilder` derives output type names as
  `"<Operation>Output"` and input objects as `"<Operation>Input"`. The `inputObject {}`
  helper hardcodes the GraphQL argument name to the literal string `"input"` (TODO to make
  configurable) — every inputObject-based operation exposes its argument as `input`.
- **`define(inputArgumentName = ...)` abuses property-delegate machinery.** It drives the
  delegate manually with `inputArgument.provideDelegate(null, String::length)` then
  `inputProvider.getValue(null, String::length)`, passing `String::length` purely as a dummy
  `KProperty`. This is harmless because the explicit `name(inputArgumentName)` call already set
  the name, so `provideDelegate`'s fallback to `property.name` (which would be `"length"`) is
  never used. A comment cites JetBrains **KT-39434**. Reads like a bug; it is deliberate.
  Anchor: `modules/graphql/sources-jvm/operations/RaptorGraphOperation.kt` (`define`).

The Raptor context is threaded into the fluid-graphql executor via a
`GExecutorContextExtensionKey` (`GExecutorContext.raptorContext`, defined in
`modules/graphql/sources-jvm/graphql/execution/RaptorExecutorContextExtensionKey.kt`). The
scalar/enum/input-object coercers and `FieldResolver` read it with `checkNotNull(...)`, so a
path that fails to install it crashes with an `IllegalStateException` (not an NPE — `checkNotNull`
throws `IllegalStateException`). `ArgumentResolver` is the exception: it reads
`execution.raptorContext ?: return null` and degrades to null instead of throwing. Raptor's
`GraphType`/`GraphField`/`GraphArgument` are likewise smuggled onto library AST nodes via
private `GNodeExtensionKey`s exposed as `raptorType`/`raptorField`/`raptorArgument` (see
`modules/graphql/sources-jvm/graphql/RaptorTypeNodeExtensionKey.kt` and siblings).
</content>
