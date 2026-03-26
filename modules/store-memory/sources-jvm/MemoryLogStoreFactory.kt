package io.fluidsonic.raptor.store.memory

import io.fluidsonic.raptor.store.*
import kotlin.reflect.*


private object MemoryLogStoreFactory : RaptorLogStoreFactory {

	override fun <Value : Any> create(name: String, valueClass: KClass<Value>): RaptorLogStore<Value> =
		MemoryLogStore()
}


public fun RaptorLogStoreFactory.Companion.memory(): RaptorLogStoreFactory =
	MemoryLogStoreFactory
