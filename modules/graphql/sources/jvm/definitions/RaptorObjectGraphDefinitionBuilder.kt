package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.jvm.*


@RaptorDsl
public class RaptorObjectGraphDefinitionBuilder<Type : Any> internal constructor(
	kotlinType: KotlinType,
	name: String,
	private val stackTrace: List<StackTraceElement>,
) : RaptorStructuredGraphTypeDefinitionBuilder<Type>(
	kotlinType = kotlinType,
	name = name
) {

	private val fieldDefinitions: MutableList<GraphFieldDefinition> = mutableListOf()
	private var hasInputObject = false


	override fun build(description: String?, additionalDefinitions: Collection<RaptorGraphDefinition>) =
		ObjectGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			fieldDefinitions = fieldDefinitions.ifEmpty { null }
				?: error("At least one field must be defined: field(…)"),
			kotlinType = kotlinType,
			name = name,
			stackTrace = stackTrace
		)


	@OptIn(ExperimentalStdlibApi::class)
	@RaptorDsl
	public inline fun <reified FieldType> field(
		name: String,
		@BuilderInference noinline configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit,
	) {
		field(name = name, type = typeOf<FieldType>(), configure = configure)
	}


	@OptIn(ExperimentalStdlibApi::class)
	@RaptorDsl
	public inline fun <reified FieldType> field(
		property: KProperty1<Type, FieldType>,
		noinline configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			property = property,
			type = typeOf<FieldType>(),
			configure = configure
		)
	}


	@RaptorDsl
	public fun <FieldType> field(
		property: KProperty1<Type, FieldType>,
		type: KType,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = property.name,
			type = type,
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { property.get(it as Type) }, // FIXME
			configure = configure
		)
	}


	@RaptorDsl
	public fun <FieldType> field(
		function: KFunction2<Type, RaptorGraphContext, FieldType>,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = function.name,
			type = function.reflect()!!.returnType, // FIXME
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { function(it as Type, context) }, // FIXME
			configure = configure
		)
	}


	@JvmName("fieldSuspend")
	@RaptorDsl
	public fun <FieldType> field(
		function: KSuspendFunction2<Type, RaptorGraphContext, FieldType>,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = function.name,
			type = function.reflect()!!.returnType, // FIXME
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { function(it as Type, context) }, // FIXME
			configure = configure
		)
	}


	@RaptorDsl
	public fun <FieldType> field(
		name: String,
		type: KType,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit,
	) {
		field(
			name = name,
			type = type,
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = null,
			configure = configure
		)
	}


	private fun <FieldType> field(
		name: String,
		type: KType,
		stackTrace: List<StackTraceElement>,
		implicitResolver: (suspend RaptorGraphOutputScope.(parent: Any) -> Any?)?,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit,
	) {
		if (fieldDefinitions.any { it.name === name })
			error("Cannot define multiple fields named '$name'.")

		fieldDefinitions += RaptorGraphFieldBuilder.WithResolver<FieldType, Type>(
			kotlinType = KotlinType.of(
				type = type,
				containingType = kotlinType,
				allowMaybe = false,
				allowNull = true,
				allowedVariance = KVariance.OUT,
				requireSpecialization = true
			),
			implicitResolver = implicitResolver,
			name = name,
			parentKotlinType = kotlinType,
			stackTrace = stackTrace
		)
			.apply(configure)
			.build()
	}


	@RaptorDsl
	public fun inputObject(
		name: String = RaptorGraphDefinition.defaultName,
		configure: RaptorInputObjectGraphDefinitionBuilder<Type>.() -> Unit,
	) {
		check(!hasInputObject) { "Cannot define multiple input objects for object '${this.name}'." }

		hasInputObject = true

		nestedDefinitions += RaptorInputObjectGraphDefinitionBuilder<Type>(
			kotlinType = kotlinType,
			name = when (name) {
				RaptorGraphDefinition.defaultName -> "${this.name}Input"
				else -> name
			},
			stackTrace = stackTrace(skipCount = 1)
		)
			.apply(configure)
	}
}
