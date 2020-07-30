package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object EnumCoercer : GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	override fun GOutputCoercerContext.coerceOutput(output: Any): Any =
		(output as? Enum<*>)?.name ?: invalidValueError()


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any? {
		val definition = (type as GEnumType).raptorTypeDefinition as GraphEnumDefinition<*>

		return (input as? String)
			?.let { name -> definition.values.firstOrNull { it.name == name } }
			?: invalidValueError()
	}
}
