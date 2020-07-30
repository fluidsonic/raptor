package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


private object ArgumentDefinitionNodeExtensionKey : GNodeExtensionKey<GraphArgumentDefinition<*>>


internal val GArgumentDefinition.raptorArgumentDefinition: GraphArgumentDefinition<*>?
	get() = extensions[ArgumentDefinitionNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GArgumentDefinition>.raptorArgumentDefinition: GraphArgumentDefinition<*>?
	get() = get(ArgumentDefinitionNodeExtensionKey)
	set(value) {
		set(ArgumentDefinitionNodeExtensionKey, value)
	}
