package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


// FIXME allow some kind of invalidValueError() in all Raptor parsers & serializers
internal object ScalarCoercer : GNodeInputCoercer<GValue>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	override fun GNodeInputCoercerContext.coerceNodeInput(input: GValue): Any? {
		val context = checkNotNull(execution.raptorContext)
		val definition = (type as GCustomScalarType).raptorTypeDefinition as GraphScalarDefinition<*>

		val parsingContext = object : RaptorGraphArgumentParsingScope, RaptorGraphScope by context {

			override fun invalid(details: String?): Nothing =
				this@coerceNodeInput.invalid(details = details)
		}

		return when (input) {
			is GBooleanValue -> definition.parseBoolean?.let { it(parsingContext, input.value) }
			is GFloatValue -> definition.parseFloat?.let { it(parsingContext, input.value) }
			is GIntValue -> definition.parseInt?.let { it(parsingContext, input.value) }
			is GObjectValue -> definition.parseObject?.let { it(parsingContext, input.unwrap()) }
			is GStringValue -> definition.parseString?.let { it(parsingContext, input.value) }
			else -> invalid()
		}
	}


	@Suppress("UNCHECKED_CAST")
	override fun GOutputCoercerContext.coerceOutput(output: Any): Any {
		val context = checkNotNull(execution.raptorContext)
		val definition = (type as GCustomScalarType).raptorTypeDefinition as GraphScalarDefinition<Any>

		return with(definition) {
			serializeJson(context, output)
		}
	}


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any? {
		val context = checkNotNull(execution.raptorContext)
		val definition = (type as GCustomScalarType).raptorTypeDefinition as GraphScalarDefinition<*>
		if (!definition.jsonInputClass.isInstance(input))
			invalid()

		val parsingContext = object : RaptorGraphArgumentParsingScope, RaptorGraphScope by context {

			override fun invalid(details: String?): Nothing =
				this@coerceVariableInput.invalid(details = details)
		}

		return with(definition) {
			parseJson(parsingContext, input)
		}
	}
}
