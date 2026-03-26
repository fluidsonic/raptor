package io.fluidsonic.raptor.store

import kotlin.reflect.*


public interface RaptorLogStoreFactory {

	public fun <Value : Any> create(
		name: String,
		valueClass: KClass<Value>,
	): RaptorLogStore<Value>

	public companion object
}


public inline fun <reified Value : Any> RaptorLogStoreFactory.create(name: String): RaptorLogStore<Value> =
	create(name = name, valueClass = Value::class)
