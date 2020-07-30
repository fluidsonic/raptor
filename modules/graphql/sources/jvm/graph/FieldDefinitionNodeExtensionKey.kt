package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


private object FieldDefinitionNodeExtensionKey : GNodeExtensionKey<GraphFieldDefinition<*, *>>


internal val GFieldDefinition.raptorFieldDefinition: GraphFieldDefinition<*, *>?
	get() = extensions[FieldDefinitionNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GFieldDefinition>.raptorFieldDefinition: GraphFieldDefinition<*, *>?
	get() = get(FieldDefinitionNodeExtensionKey)
	set(value) {
		set(FieldDefinitionNodeExtensionKey, value)
	}
