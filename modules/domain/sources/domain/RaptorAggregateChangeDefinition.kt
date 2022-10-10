package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlin.reflect.full.*


public data class RaptorAggregateChangeDefinition<out Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
	val changeClass: KClass<Change>,
	val discriminator: String,
) {

	public val type: KType = changeClass.starProjectedType
}
