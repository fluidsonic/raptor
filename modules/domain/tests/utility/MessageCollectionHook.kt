import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*


internal class MessageCollectionHook(
	override val aggregateIdClassFilter: Set<KClass<out RaptorAggregateId>>? = null,
	override val projectionIdClassFilter: Set<KClass<out RaptorAggregateProjectionId>>? = null,
) : RaptorDomainStreamHook {

	val events = mutableListOf<RaptorAggregateEvent<*, *>>()
	val projectionEvents = mutableListOf<RaptorAggregateProjectionEvent<*, *, *>>()
	var replayCompletedCount = 0
		private set


	override fun onAggregateEvent(event: RaptorAggregateEvent<*, *>) {
		events += event
	}


	override fun onAggregateProjectionEvent(event: RaptorAggregateProjectionEvent<*, *, *>) {
		projectionEvents += event
	}


	override fun onReplayCompleted() {
		replayCompletedCount++
	}
}
