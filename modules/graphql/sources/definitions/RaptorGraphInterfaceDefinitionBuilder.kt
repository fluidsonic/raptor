package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
class RaptorGraphInterfaceDefinitionBuilder<Value : Any> internal constructor(
	private val stackTrace: List<StackTraceElement>,
	private val valueClass: KClass<Value>,
	private val defaultName: (() -> String?)? = null
) {

	private var description: String? = null
	private val fields = mutableListOf<GraphInterfaceDefinition.Field<Value, *>>()
	private var name: String? = null


	init {
		checkGraphCompatibility(valueClass)
	}


	internal fun build() =
		GraphInterfaceDefinition(
			description = description,
			fields = fields.ifEmpty { null }
				?: error("At least one field must be defined: field(…) { … }"),
			name = name ?: defaultName?.invoke() ?: valueClass.defaultGraphName(),
			stackTrace = stackTrace,
			valueClass = valueClass
		)


	@RaptorDsl
	fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}


	@OptIn(ExperimentalStdlibApi::class)
	@RaptorDsl
	inline fun <reified FieldValue> field(
		name: String,
		noinline configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) =
		field(name = name, valueType = typeOf<FieldValue>(), configure = configure)


	@RaptorDsl
	fun <FieldValue> field(
		property: KProperty1<Value, FieldValue>,
		configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) =
		field(name = property.name, valueType = property.returnType, configure = configure)


	@RaptorDsl
	fun <FieldValue> field(
		function: KSuspendFunction2<Value, RaptorGraphScope, FieldValue>,
		configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) =
		field(name = function.name, valueType = function.returnType, configure = configure)


	@RaptorDsl
	fun <FieldValue> field(
		name: String,
		valueType: KType,
		configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) {
		if (fields.any { it.name === name })
			error("Cannot define multiple fields named '$name'.")

		fields += FieldBuilder<FieldValue>(
			name = name,
			valueType = valueType
		)
			.apply(configure)
			.build()
	}


	@RaptorDsl
	fun name(name: String) {
		check(this.name === null) { "Cannot define multiple names." }

		this.name = name
	}


	@RaptorDsl
	inner class FieldBuilder<FieldValue> internal constructor(
		private val name: String,
		private val valueType: KType,
		private val argumentsContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl()
	) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentsContainer {

		private var description: String? = null
		private var isNullable = valueType.isMarkedNullable


		init {
			checkGraphCompatibility(valueType)
		}


		internal fun build() =
			GraphInterfaceDefinition.Field<Value, FieldValue>(
				arguments = argumentsContainer.arguments,
				description = description,
				name = name,
				valueType = valueType.withNullability(isNullable) // FIXME get rid of full reflect
			)


		@RaptorDsl
		fun description(description: String) {
			check(this.description === null) { "Cannot define the description more than once." }

			this.description = description
		}
	}
}
