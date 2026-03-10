import io.fluidsonic.raptor.domain.*


internal class MessageCollectionHook : RaptorDomainStreamHook {

	val messages = mutableListOf<RaptorAggregateStreamMessage<*, *>>()
	val projectionMessages = mutableListOf<RaptorAggregateProjectionStreamMessage<*, *, *>>()


	override fun onAggregateStreamMessage(message: RaptorAggregateStreamMessage<*, *>) {
		messages += message
	}


	override fun onAggregateProjectionStreamMessage(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {
		projectionMessages += message
	}
}
