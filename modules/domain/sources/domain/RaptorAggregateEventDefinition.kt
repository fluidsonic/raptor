package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public data class RaptorAggregateEventDefinition<out Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>>(
	val discriminator: String,
	val eventClass: KClass<Event>,
)
