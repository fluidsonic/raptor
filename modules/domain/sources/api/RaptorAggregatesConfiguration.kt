package io.fluidsonic.raptor.domain


public class RaptorAggregatesConfiguration internal constructor(
	public val definitions: RaptorAggregateDefinitions,
	public val eventFactory: RaptorAggregateEventFactory,
	internal val eventStreamInternal: DefaultAggregateEventStream,
	internal val manager: RaptorAggregateManager,
	internal val projectionEventStreamInternal: DefaultAggregateProjectionEventStream,
	internal val projectionLoaderManager: RaptorAggregateProjectorLoaderManager,
	public val store: RaptorAggregateStore,
) {

	public val eventStream: RaptorAggregateEventStream
		get() = eventStreamInternal


	public val projectionEventStream: RaptorAggregateProjectionEventStream
		get() = projectionEventStreamInternal
}
