package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorObjectExtensionGraphDefinitionBuilder<Type : Any> internal constructor(
	private val kotlinType: KotlinType,
	private val stackTrace: List<StackTraceElement>,
) {

	private val fieldDefinitions: MutableList<GraphFieldDefinition> = mutableListOf()


	internal fun build() =
		ObjectExtensionGraphDefinition(
			additionalDefinitions = emptyList(),
			fieldDefinitions = fieldDefinitions.ifEmpty { null }
				?: error("At least one field must be defined: field(…)"),
			kotlinType = kotlinType,
			stackTrace = stackTrace
		)


	@RaptorDsl
	public inline fun <reified FieldType> field(
		name: String,
		@BuilderInference noinline configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit,
	) {
		field(name = name, type = typeOf<FieldType>(), configure = configure)
	}


	@RaptorDsl
	public fun <FieldType> field(
		property: KProperty1<Type, FieldType>,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = property.name,
			type = property.returnType, // TODO
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { property.get(it as Type) }, // TODO
			configure = configure
		)
	}


	@RaptorDsl
	public fun <FieldType> field(
		function: KSuspendFunction2<Type, RaptorGraphContext, FieldType>,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = function.name,
			type = function.returnType,
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { function(it as Type, context) }, // TODO
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
}
