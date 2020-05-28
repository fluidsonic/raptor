package io.fluidsonic.raptor

import kotlin.reflect.*


// FIXME customize value definitions
@RaptorDsl
class RaptorGraphEnumDefinitionBuilder<Value : Enum<Value>> internal constructor(
	private val stackTrace: List<StackTraceElement>,
	private val valueClass: KClass<Value>,
	private val values: List<Value>,
	private val defaultName: (() -> String?)? = null
) {

	private var description: String? = null
	private var name: String? = null


	init {
		checkGraphCompatibility(valueClass)
	}


	internal fun build() =
		GraphEnumDefinition(
			description = description,
			name = name ?: defaultName?.invoke() ?: valueClass.defaultGraphName(),
			stackTrace = stackTrace,
			valueClass = valueClass,
			values = values
		)


	@RaptorDsl
	fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}


	@RaptorDsl
	fun name(name: String) {
		check(this.name === null) { "Cannot define multiple names." }

		this.name = name
	}
}
