package io.fluidsonic.raptor.store.memory

import io.fluidsonic.raptor.store.*
import java.util.concurrent.*


internal class MemoryLogStore<Value : Any> : RaptorLogStore<Value> {

	private val queue = ConcurrentLinkedQueue<Value>()


	override suspend fun append(value: Value) {
		queue.add(value)
	}


	fun values(): List<Value> =
		queue.toList()
}
