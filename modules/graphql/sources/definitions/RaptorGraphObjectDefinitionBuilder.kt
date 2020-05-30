package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
class RaptorGraphObjectDefinitionBuilder<Value : Any> internal constructor(
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	defaultName: (() -> String?)? = null
) : RaptorGraphStructuredTypeDefinitionBuilder<Value, GraphObjectDefinition<Value>>(
	defaultName = defaultName,
	valueClass = valueClass
) {

	private val fields: MutableList<GraphObjectDefinition.Field<Value, *>> = mutableListOf()


	override fun build(description: String?, name: String, nestedDefinitions: List<GraphNamedTypeDefinition<*>>) =
		GraphObjectDefinition(
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
		@BuilderInference noinline configure: FieldBuilder<FieldValue>.() -> Unit
	) =
		field(name = name, valueType = typeOf<FieldValue>(), configure = configure)


	@RaptorDsl
	fun <FieldValue> field(
		property: KProperty1<Value, FieldValue>,
		configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) {
		val name = property.name

		if (fields.any { it.name === name })
			error("Cannot define multiple fields named '$name'.")

		fields += FieldBuilder<FieldValue>(
			name = name,
			valueType = property.returnType,
			implicitResolver = { property.get(it) }
		)
			.apply(configure)
			.build()
	}


	@RaptorDsl
	fun <FieldValue> field(
		function: KSuspendFunction2<Value, RaptorGraphContext, FieldValue>,
		configure: FieldBuilder<FieldValue>.() -> Unit = {}
	) {
		val name = function.name

		if (fields.any { it.name === name })
			error("Cannot define multiple fields named '$name'.")

		fields += FieldBuilder<FieldValue>(
			name = name,
			valueType = function.returnType,
			implicitResolver = { function.invoke(it, context) }
		)
			.apply(configure)
			.build()
	}


	@RaptorDsl
	fun <FieldValue> field(
		name: String,
		valueType: KType,
		configure: FieldBuilder<FieldValue>.() -> Unit
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
		implicitResolver: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = null,
		private val argumentsContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl()
	) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentsContainer {

		private var description: String? = null
		private var isImplicitResolver = implicitResolver !== null
		private var isNullable = valueType.isMarkedNullable
		private var resolver: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = implicitResolver


		init {
			checkGraphCompatibility(valueType)
		}


		internal fun build() =
			GraphObjectDefinition.Field(
				arguments = argumentsContainer.arguments,
				description = description,
				name = name,
				resolver = resolver,
				valueType = valueType.withNullability(isNullable)
			)


		@RaptorDsl
		fun description(description: String) {
			check(this.description === null) { "Cannot define the description more than once." }

			this.description = description
		}


		@RaptorDsl
		fun resolver(resolver: suspend RaptorGraphScope.(parent: Value) -> FieldValue) {
			check(this.resolver === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." }

			this.isImplicitResolver = false
			this.resolver = resolver
		}


		// remove once fixed: https://youtrack.jetbrains.com/issue/KT-36371
		@RaptorDsl
		fun resolverNullable(resolver: suspend RaptorGraphScope.(parent: Value) -> FieldValue?) {
			check(this.resolver === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." }

			this.isImplicitResolver = false
			this.isNullable = true
			this.resolver = resolver
		}
	}
}
