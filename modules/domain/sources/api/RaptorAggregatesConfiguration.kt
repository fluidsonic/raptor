package io.fluidsonic.raptor.domain


public class RaptorAggregatesConfiguration internal constructor(
	public val definitions: RaptorAggregateDefinitions,
	public val eventFactory: RaptorAggregateEventFactory,
	internal val eventStreamInternal: DefaultAggregateStream,
	internal val manager: RaptorAggregateManager,
	internal val projectionEventStreamInternal: DefaultAggregateProjectionStream,
	internal val projectionLoaderManager: RaptorAggregateProjectorLoaderManager,
	public val store: RaptorAggregateStore,
) {

	public val eventStream: RaptorAggregateStream
		get() = eventStreamInternal


	public val projectionEventStream: RaptorAggregateProjectionEventStream
		get() = projectionEventStreamInternal
}
