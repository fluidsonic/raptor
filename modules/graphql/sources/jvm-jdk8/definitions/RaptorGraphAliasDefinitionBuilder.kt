package io.fluidsonic.raptor

import kotlin.reflect.*


@RaptorDsl
class RaptorGraphAliasDefinitionBuilder<Value : Any, ExternalValue : Any> internal constructor(
	private var isId: Boolean,
	private val referencedValueClass: KClass<ExternalValue>,
	private val stackTrace: List<StackTraceElement>,
	private val valueClass: KClass<Value>
) {

	private var parse: (RaptorGraphScope.(input: ExternalValue) -> Value?)? = null
	private var serialize: (RaptorGraphScope.(value: Value) -> ExternalValue)? = null

	init {
		checkGraphCompatibility(valueClass)
		checkGraphCompatibility(referencedValueClass)

		check(!isId || referencedValueClass == String::class) { "An ID alias must reference value class String." }
	}


	internal fun build() =
		GraphAliasDefinition(
			isId = isId,
			parse = checkNotNull(parse) { "Parsing must be defined: parse { … }" },
			referencedValueClass = referencedValueClass,
			serialize = checkNotNull(serialize) { "Serializing must be defined: serialize { … }" },
			stackTrace = stackTrace,
			valueClass = valueClass
		)


	@RaptorDsl
	fun parse(parse: RaptorGraphScope.(externalValue: ExternalValue) -> Value?) {
		check(this.parse === null) { "Cannot define multiple parsers." }

		this.parse = parse
	}


	@RaptorDsl
	fun serialize(serialize: RaptorGraphScope.(value: Value) -> ExternalValue) {
		check(this.serialize === null) { "Cannot define multiple serializers." }

		this.serialize = serialize
	}
}
