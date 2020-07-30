package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object InputObjectCoercer : GVariableInputCoercer<Map<String, Any?>> {

	override fun GVariableInputCoercerContext.coerceVariableInput(input: Map<String, Any?>): Any? {
		val context = checkNotNull(execution.raptorContext)
		val definition = (type as GInputObjectType).raptorTypeDefinition as GraphInputObjectDefinition<*>

		return definition.factory(context)
	}
}
