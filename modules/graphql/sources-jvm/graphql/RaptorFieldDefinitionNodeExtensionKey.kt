package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*


private object RaptorFieldDefinitionNodeExtensionKey : GNodeExtensionKey<GraphField>


internal val GFieldDefinition.raptorField: GraphField?
	get() = extensions[RaptorFieldDefinitionNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GFieldDefinition>.raptorField: GraphField?
	get() = get(RaptorFieldDefinitionNodeExtensionKey)
	set(value) {
		set(RaptorFieldDefinitionNodeExtensionKey, value)
	}
