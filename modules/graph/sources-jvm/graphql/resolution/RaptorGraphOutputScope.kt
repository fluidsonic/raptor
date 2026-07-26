package io.fluidsonic.raptor.graph


/**
 * The scope in which a value is serialized for GraphQL output, i.e. a scalar's or enum's `serialize { … }` and an
 * alias' `serialize { … }`.
 *
 * Serialization is a pure transformation of the value it is given and has no access to the surrounding transaction.
 * Field resolvers, which do, run in a [RaptorGraphResolverScope] instead.
 */
public interface RaptorGraphOutputScope
