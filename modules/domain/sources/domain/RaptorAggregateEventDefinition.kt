package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlin.reflect.full.*


public data class RaptorAggregateEventDefinition<out Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>>(
	val discriminator: String,
	val eventClass: KClass<Event>,
) {

	public val type: KType = eventClass.starProjectedType
}
