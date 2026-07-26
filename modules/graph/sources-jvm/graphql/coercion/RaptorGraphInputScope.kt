package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


/**
 * The scope in which GraphQL input is turned into a Kotlin value, i.e. a scalar's or enum's `parse { … }`, an alias'
 * `parse { … }`, an input object's `factory { … }` and an argument's `map`/`validate` transforms.
 *
 * Parsing is a pure transformation of the value it is given and has no access to the surrounding transaction.
 */
public interface RaptorGraphInputScope {

	// TODO We can throw a special exception that the graph system catches to add contextual information.
	//      That way we can support it deep inside code and still have that information.
	/** Rejects the input being parsed, surfacing [details] to the client as an `invalid value` error. */
	@RaptorDsl
	public fun invalid(details: String? = null): Nothing
}
