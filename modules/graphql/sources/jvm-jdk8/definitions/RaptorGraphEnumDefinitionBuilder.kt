package io.fluidsonic.raptor

import kotlin.reflect.*


// FIXME customize value definitions
@RaptorDsl
class RaptorGraphEnumDefinitionBuilder<Value : Enum<Value>> internal constructor(
	name: String,
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	private val values: List<Value>
) : RaptorGraphNamedTypeDefinitionBuilder<Value, GraphEnumDefinition<Value>>(
	name = name,
	valueClass = valueClass
) {

	override fun build(description: String?) =
		GraphEnumDefinition(
			description = description,
			name = name,
			stackTrace = stackTrace,
			valueClass = valueClass,
			values = values
		)
}
