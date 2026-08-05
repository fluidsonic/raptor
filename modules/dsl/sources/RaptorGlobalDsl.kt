package io.fluidsonic.raptor


public object RaptorGlobalDsl


/**
 * Entry point for Raptor's global DSL functions, e.g. `RaptorGlobalDsl.bson`.
 *
 * Careful: an assembled `Raptor` instance is conventionally also named `raptor` in this codebase. Inside a
 * scope where a local `val raptor: Raptor` exists, `raptor.bson.…` fails to resolve ("Unresolved reference:
 * bson") because the local shadows this global. Qualify as `RaptorGlobalDsl.bson.…` instead, or rename the
 * local variable.
 */
@RaptorDsl
public val raptor: RaptorGlobalDsl = RaptorGlobalDsl
