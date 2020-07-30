package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*


@RaptorDsl
class RaptorGraphInterfaceDefinitionBuilder<Value : Any> internal constructor(
	name: String,
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : RaptorGraphStructuredTypeDefinitionBuilder<Value, GraphInterfaceDefinition<Value>>(
	name = name,
	valueClass = valueClass
) {

	private val fields: MutableList<GraphFieldDefinition<Value, *>> = mutableListOf()


	override fun build(description: String?, nestedDefinitions: List<GraphNamedTypeDefinition<*>>) =
		GraphInterfaceDefinition(
			description = description,
			fields = fields.ifEmpty { null }
				?: error("At least one field must be defined: field(…) { … }"),
			name = name,
			nestedDefinitions = nestedDefinitions,
			stackTrace = stackTrace,
			valueClass = valueClass
		)


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
		function: KSuspendFunction2<Value, RaptorGraphContext, FieldValue>,
		configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) =
		field(name = function.name, valueType = function.reflect()!!.returnType, configure = configure) // FIXME


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
	inner class FieldBuilder<FieldValue> internal constructor(
		private val name: String,
		private val valueType: KType,
		private val argumentsContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
			factoryName = "-"
		)
	) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentsContainer {

		private var description: String? = null
		private var isNullable = valueType.isMarkedNullable


		init {
			checkGraphCompatibility(valueType)
		}


		internal fun build() =
			GraphFieldDefinition<Value, FieldValue>(
				arguments = argumentsContainer.arguments,
				description = description,
				name = name,
				resolver = null,
				valueType = valueType.withNullability(isNullable) // FIXME get rid of full reflect
			)


		@RaptorDsl
		fun description(description: String) {
			check(this.description === null) { "Cannot define the description more than once." }

			this.description = description
		}
	}
}
