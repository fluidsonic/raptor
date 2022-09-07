package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


private object RaptorArgumentNodeExtensionKey : GNodeExtensionKey<GraphArgument>


internal val GArgumentDefinition.raptorArgument: GraphArgument?
	get() = extensions[RaptorArgumentNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GArgumentDefinition>.raptorArgument: GraphArgument?
	get() = get(RaptorArgumentNodeExtensionKey)
	set(value) {
		set(RaptorArgumentNodeExtensionKey, value)
	}
