package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*


@RaptorDsl
class RaptorGraphObjectDefinitionBuilder<Value : Any> internal constructor(
	name: String,
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : RaptorGraphStructuredTypeDefinitionBuilder<Value, GraphObjectDefinition<Value>>(
	name = name,
	valueClass = valueClass
) {

	private val fields: MutableList<GraphFieldDefinition<Value, *>> = mutableListOf()
	private var hasInputObject = false


	override fun build(description: String?, nestedDefinitions: List<GraphNamedTypeDefinition<*>>) =
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

		val reflection = function.reflect()
			?: error("Reflection is not available.") // FIXME improve this. probably just use reified Value

		fields += FieldBuilder<FieldValue>(
			name = name,
			valueType = reflection.returnType,
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
	fun inputObject(
		name: String = RaptorGraphDefinition.defaultName,
		configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
	) {
		check(!hasInputObject) { "Cannot define multiple input objects for object '${this.name}'." }

		hasInputObject = true

		nestedDefinitions += RaptorGraphInputObjectDefinitionBuilder(
			name = when (name) {
				RaptorGraphDefinition.defaultName -> "${this.name}Input"
				else -> name
			},
			stackTrace = stackTrace(skipCount = 1),
			valueClass = valueClass
		)
			.apply(configure)
	}


	@RaptorDsl
	inner class FieldBuilder<FieldValue> internal constructor(
		private val name: String,
		private val valueType: KType,
		implicitResolver: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = null,
		private val argumentsContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
			factoryName = "resolver"
		)
	) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentsContainer {

		private var description: String? = null
		private var isImplicitResolver = implicitResolver !== null
		private var isNullable = valueType.isMarkedNullable
		private var resolver: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = implicitResolver


		init {
			checkGraphCompatibility(valueType)
		}


		internal fun build(): GraphFieldDefinition<Value, FieldValue?> =
			GraphFieldDefinition(
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
			check(this.resolver === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." } // FIXME mark KProperty resolvers implicit

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
