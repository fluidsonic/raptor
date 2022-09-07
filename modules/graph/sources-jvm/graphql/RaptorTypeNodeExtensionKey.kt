package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


private object RaptorTypeNodeExtensionKey : GNodeExtensionKey<GraphType>


internal val GArgumentDefinition.raptorType: GraphType?
	get() = extensions[RaptorTypeNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GArgumentDefinition>.raptorType: GraphType?
	@JvmName("getForArgumentDefinition")
	get() = get(RaptorTypeNodeExtensionKey)
	@JvmName("setForArgumentDefinition")
	set(value) {
		set(RaptorTypeNodeExtensionKey, value)
	}


internal val GFieldDefinition.raptorType: GraphType?
	get() = extensions[RaptorTypeNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GFieldDefinition>.raptorType: GraphType?
	@JvmName("getForFieldDefinition")
	get() = get(RaptorTypeNodeExtensionKey)
	@JvmName("setForFieldDefinition")
	set(value) {
		set(RaptorTypeNodeExtensionKey, value)
	}

internal val GNamedType.raptorType: GraphType?
	get() = extensions[RaptorTypeNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GNamedType>.raptorType: GraphType?
	get() = get(RaptorTypeNodeExtensionKey)
	set(value) {
		set(RaptorTypeNodeExtensionKey, value)
	}
