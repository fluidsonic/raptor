package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.*


internal class RaptorAggregateProjectionEventStreamImpl(
	private val logger: Logger,
) : RaptorAggregateProjectionEventStream {

	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> {
		TODO("Not yet implemented")
	}


	override fun <Id, Event : RaptorAggregateEvent<Id>, Projection : RaptorProjection<Id>> subscribeIn(
		scope: CoroutineScope,
		collector: suspend (event: RaptorAggregateProjectionEvent<Id, Event, Projection>) -> Unit,
		errorStrategy: RaptorAggregateProjectionEventStream.ErrorStrategy,
		eventClass: KClass<Event>,
		idClass: KClass<Id>,
		projectionClass: KClass<Projection>,
	): Job where Id : RaptorAggregateId, Id : RaptorProjectionId {
		var failedProjectionIds: MutableSet<RaptorProjectionId>? = null

		return asFlow()
			.filterIsInstance(idClass = idClass, projectionClass = projectionClass, eventClass = eventClass)
			.onEach { event ->
				val projectionId = event.projection.id

				if (failedProjectionIds?.contains(event.projection.id) == true)
					return@onEach logger.error("Cannot process event for projection $projectionId because of a previous error.")

				try {
					collector(event)
				}
				catch (e: CancellationException) {
					throw e
				}
				catch (e: Throwable) {
					(failedProjectionIds ?: hashSetOf<RaptorProjectionId>().also { failedProjectionIds = it })
						.add(projectionId)
				}
			}
			.launchIn(scope)
	}
}
