package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public data class RaptorAggregateCommandDefinition<in Id : RaptorAggregateId, Command : RaptorAggregateCommand<Id>>(
	val commandClass: KClass<Command>,
)
