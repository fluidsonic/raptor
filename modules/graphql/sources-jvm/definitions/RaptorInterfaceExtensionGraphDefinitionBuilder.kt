package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorInterfaceExtensionGraphDefinitionBuilder<Type : Any> internal constructor(
	private val kotlinType: KotlinType,
	private val stackTrace: List<StackTraceElement>,
) {

	private val fieldDefinitions: MutableList<GraphFieldDefinition> = mutableListOf()


	internal fun build() =
		InterfaceExtensionGraphDefinition(
			additionalDefinitions = emptyList(),
			fieldDefinitions = fieldDefinitions.ifEmpty { null }
				?: error("At least one field must be defined: field(…)"),
			kotlinType = kotlinType,
			stackTrace = stackTrace
		)


	@RaptorDsl
	public inline fun <reified FieldType> field(
		name: String,
		noinline configure: RaptorGraphFieldBuilder.() -> Unit,
	) {
		field(name = name, type = typeOf<FieldType>(), configure = configure)
	}


	@RaptorDsl
	@Suppress("NOTHING_TO_INLINE")
	public inline fun <FieldType> field(
		property: KProperty1<Type, FieldType>,
		noinline configure: RaptorGraphFieldBuilder.() -> Unit = {},
	) {
		field(name = property.name, type = property.returnType, configure = configure) // TODO
	}


	@RaptorDsl
	@Suppress("NOTHING_TO_INLINE")
	public inline fun <FieldType> field(
		noinline function: KSuspendFunction2<Type, RaptorGraphContext, FieldType>,
		noinline configure: RaptorGraphFieldBuilder.() -> Unit = {},
	) {
		field(name = function.name, type = function.returnType, configure = configure)
	}


	@RaptorDsl
	public fun field(
		name: String,
		type: KType,
		configure: RaptorGraphFieldBuilder.() -> Unit,
	) {
		if (fieldDefinitions.any { it.name === name })
			error("Cannot define multiple fields named '$name'.")

		fieldDefinitions += RaptorGraphFieldBuilder(
			kotlinType = KotlinType.of(
				type = type,
				containingType = kotlinType,
				allowMaybe = false,
				allowNull = true,
				allowedVariance = KVariance.OUT,
				requireSpecialization = true
			),
			name = name,
			parentKotlinType = kotlinType,
			stackTrace = stackTrace(skipCount = 1)
		)
			.apply(configure)
			.build()
	}
}
