package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


private object TypeDefinitionNodeExtensionKey : GNodeExtensionKey<GraphTypeDefinition<*>>


internal val GArgumentDefinition.raptorTypeDefinition: GraphTypeDefinition<*>?
	get() = extensions[TypeDefinitionNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GArgumentDefinition>.raptorTypeDefinition: GraphTypeDefinition<*>?
	@JvmName("getForArgumentDefinition")
	get() = get(TypeDefinitionNodeExtensionKey)
	@JvmName("setForArgumentDefinition")
	set(value) {
		set(TypeDefinitionNodeExtensionKey, value)
	}


internal val GFieldDefinition.raptorTypeDefinition: GraphTypeDefinition<*>?
	get() = extensions[TypeDefinitionNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GFieldDefinition>.raptorTypeDefinition: GraphTypeDefinition<*>?
	@JvmName("getForFieldDefinition")
	get() = get(TypeDefinitionNodeExtensionKey)
	@JvmName("setForFieldDefinition")
	set(value) {
		set(TypeDefinitionNodeExtensionKey, value)
	}

internal val GNamedType.raptorTypeDefinition: GraphTypeDefinition<*>?
	get() = extensions[TypeDefinitionNodeExtensionKey]


internal var GNodeExtensionSet.Builder<GNamedType>.raptorTypeDefinition: GraphTypeDefinition<*>?
	get() = get(TypeDefinitionNodeExtensionKey)
	set(value) {
		set(TypeDefinitionNodeExtensionKey, value)
	}
