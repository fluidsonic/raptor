package io.fluidsonic.raptor.store.memory

import io.fluidsonic.raptor.store.*
import kotlin.reflect.*


private object MemoryLogStoreFactory : RaptorLogStoreFactory {

	override fun <Value : Any> create(name: String, valueClass: KClass<Value>): RaptorLogStore<Value> =
		MemoryLogStore()
}


/** Creates a [RaptorLogStoreFactory] that stores entries in memory. Data is not persisted and will be lost when the process exits. */
public fun RaptorLogStoreFactory.Companion.memory(): RaptorLogStoreFactory =
	MemoryLogStoreFactory
