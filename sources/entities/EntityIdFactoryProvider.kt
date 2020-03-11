package io.fluidsonic.raptor


internal interface EntityIdFactoryProvider {

	fun idFactoryForType(type: String): EntityId.Factory<*>?
}
