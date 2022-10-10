package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public data class RaptorAggregateProjectionDefinition<
	Projection : RaptorAggregateProjection<Id>,
	Id : RaptorAggregateProjectionId,
	Event : RaptorAggregateChange<Id>,
	>(
	val factory: (() -> RaptorAggregateProjector.Incremental<Projection, Id, Event>)? = null, // FIXME Use type.
	val idClass: KClass<Id>,
	val projectionClass: KClass<Projection>,
)
