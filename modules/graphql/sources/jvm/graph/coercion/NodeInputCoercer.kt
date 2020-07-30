package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.stdlib.*


internal object NodeInputCoercer : GNodeInputCoercer<Any?> {

	override fun GNodeInputCoercerContext.coerceNodeInput(input: Any?): Any? {
		val kotlinType = argumentDefinition?.raptorArgumentDefinition?.valueType
		val expectsMaybe = kotlinType?.classifier == Maybe::class
		if (expectsMaybe && input == null)
			return Maybe.nothing

		return next()
	}
}
