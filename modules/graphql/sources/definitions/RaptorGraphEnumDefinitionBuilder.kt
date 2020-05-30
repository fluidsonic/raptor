package io.fluidsonic.raptor

import kotlin.reflect.*


// FIXME customize value definitions
@RaptorDsl
class RaptorGraphEnumDefinitionBuilder<Value : Enum<Value>> internal constructor(
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	private val values: List<Value>,
	defaultName: (() -> String?)? = null
) : RaptorGraphNamedTypeDefinitionBuilder<Value, GraphEnumDefinition<Value>>(
	defaultName = defaultName,
	valueClass = valueClass
) {

	override fun build(description: String?, name: String) =
		GraphEnumDefinition(
			description = description,
			name = name,
			stackTrace = stackTrace,
			valueClass = valueClass,
			values = values
		)
}
