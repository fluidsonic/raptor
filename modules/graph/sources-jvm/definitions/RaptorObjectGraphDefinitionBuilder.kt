package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*


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


	override fun build(description: String?, additionalDefinitions: Collection<RaptorGraphDefinition>): ObjectGraphDefinition {
		val fieldDefinitions = fieldDefinitions.toMutableList()
		if (fieldDefinitions.isEmpty()) {
			when (kotlinType.classifier.objectInstance) {
				null -> error("At least one field must be defined: field(…)")
				else -> fieldDefinitions += GraphFieldDefinition.Resolvable(
					argumentDefinitions = emptyList(),
					argumentResolver = ArgumentResolver(factoryName = "field"),
					description = "Dummy. See https://github.com/graphql/graphql-spec/issues/568",
					kotlinType = KotlinType(classifier = Unit::class, isNullable = false),
					name = "_",
					resolve = {},
					stackTrace = stackTrace,
				)
			}
		}

		return ObjectGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			fieldDefinitions = fieldDefinitions,
			kotlinType = kotlinType,
			name = name,
			stackTrace = stackTrace
		)
	}


	@RaptorDsl
	public inline fun <reified FieldType> field(
		name: String,
		@BuilderInference noinline configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit,
	) {
		field(name = name, type = typeOf<FieldType>(), configure = configure)
	}


	@RaptorDsl
	public inline fun <reified FieldType> field(
		property: KProperty0<FieldType>,
		noinline configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			property = property,
			type = typeOf<FieldType>(),
			configure = configure,
		)
	}


	@RaptorDsl
	public fun <FieldType> field(
		property: KProperty0<FieldType>,
		type: KType,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = property.name,
			type = type,
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { property.get() },
			configure = configure,
		)
	}


	@RaptorDsl
	public inline fun <reified FieldType> field(
		property: KProperty1<in Type, FieldType>,
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
		property: KProperty1<in Type, FieldType>,
		type: KType,
		configure: RaptorGraphFieldBuilder.WithResolver<FieldType, Type>.() -> Unit = {},
	) {
		field(
			name = property.name,
			type = type,
			stackTrace = stackTrace(skipCount = 1),
			implicitResolver = { property.get(it as Type) }, // TODO
			configure = configure
		)
	}


	@RaptorDsl
	public fun <FieldType> field(
		function: KFunction2<Type, RaptorTransactionContext, FieldType>,
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


	@JvmName("fieldSuspend")
	@RaptorDsl
	public fun <FieldType> field(
		function: KSuspendFunction2<Type, RaptorTransactionContext, FieldType>,
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

		if (type.classifier == RaptorUnion2::class)
			nestedDefinitions += RaptorUnionGraphDefinitionBuilder<RaptorUnion2<*, *>>(
				kotlinType = KotlinType.of(
					type = type.withNullability(false),
					containingType = null,
					allowMaybe = false,
					allowNull = false,
					allowedVariance = KVariance.OUT,
					requireSpecialization = true,
				),
				name = "${this.name}_$name",
				stackTrace = stackTrace,
			)
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
