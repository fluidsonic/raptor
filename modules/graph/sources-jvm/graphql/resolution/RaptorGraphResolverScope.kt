package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.transactions.*


/**
 * The scope in which a field's or operation's `resolver { … }` runs.
 *
 * Unlike plain output serialization it has access to the surrounding transaction and thus to dependency injection.
 */
public interface RaptorGraphResolverScope : RaptorGraphOutputScope, RaptorTransactionScope
