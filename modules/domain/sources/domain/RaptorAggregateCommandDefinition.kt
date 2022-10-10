package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlin.reflect.full.*


public data class RaptorAggregateCommandDefinition<in Id : RaptorAggregateId, Command : RaptorAggregateCommand<Id>>(
	val commandClass: KClass<Command>,
) {

	val type: KType = commandClass.starProjectedType
}
