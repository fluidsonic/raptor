import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*


internal class MessageCollectionHook(
	override val aggregateIdClassFilter: Set<KClass<out RaptorAggregateId>>? = null,
	override val projectionIdClassFilter: Set<KClass<out RaptorAggregateProjectionId>>? = null,
) : RaptorDomainStreamHook {

	val messages = mutableListOf<RaptorAggregateStreamMessage<*, *>>()
	val projectionMessages = mutableListOf<RaptorAggregateProjectionStreamMessage<*, *, *>>()


	override fun onAggregateStreamMessage(message: RaptorAggregateStreamMessage<*, *>) {
		messages += message
	}


	override fun onAggregateProjectionStreamMessage(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {
		projectionMessages += message
	}
}
