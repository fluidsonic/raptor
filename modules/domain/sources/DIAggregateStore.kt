package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


// FIXME improve
internal class DIAggregateStore(
	private val context: RaptorContext,
) : RaptorAggregateStore {

	private val delegate: RaptorAggregateStore by lazy { context.di.get() }


	override suspend fun add(events: List<RaptorEvent<*, *>>) {
		delegate.add(events)
	}


	override fun load() =
		delegate.load()
}
