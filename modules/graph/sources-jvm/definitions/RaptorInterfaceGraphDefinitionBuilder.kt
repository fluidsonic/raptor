package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorInterfaceGraphDefinitionBuilder<Type : Any> internal constructor(
	kotlinType: KotlinType,
	name: String,
	private val stackTrace: List<StackTraceElement>,
) : RaptorStructuredGraphTypeDefinitionBuilder<Type>(
	kotlinType = kotlinType,
	name = name
) {

	private val fieldDefinitions: MutableList<GraphFieldDefinition> = mutableListOf()


	override fun build(description: String?, additionalDefinitions: Collection<RaptorGraphDefinition>) =
		InterfaceGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			fieldDefinitions = fieldDefinitions.ifEmpty { null }
				?: error("At least one field must be defined: field(…)"),
			kotlinType = kotlinType,
			name = name,
			stackTrace = stackTrace
		)


	@RaptorDsl
	public inline fun <reified FieldType> field(
		name: String,
		noinline configure: RaptorGraphFieldBuilder.() -> Unit = {},
	) {
		field(name = name, type = typeOf<FieldType>(), configure = configure)
	}


	@RaptorDsl
	public inline fun <reified FieldType> field(
		property: KProperty1<Type, FieldType>,
		noinline configure: RaptorGraphFieldBuilder.() -> Unit = {},
	) {
		field(name = property.name, type = typeOf<FieldType>(), configure = configure)
	}


	@RaptorDsl
	public inline fun <reified FieldType> field(
		function: KSuspendFunction2<Type, RaptorTransactionContext, FieldType>,
		noinline configure: RaptorGraphFieldBuilder.() -> Unit = {},
	) {
		field(name = function.name, type = typeOf<FieldType>(), configure = configure) // TODO
	}


	@RaptorDsl
	public fun field(
		name: String,
		type: KType,
		configure: RaptorGraphFieldBuilder.() -> Unit = {},
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
