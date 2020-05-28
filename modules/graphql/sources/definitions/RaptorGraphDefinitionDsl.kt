package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
inline fun <reified Value : Any, reified ReferencedValue : Any> graphAliasDefinition(
	@BuilderInference noinline configure: RaptorGraphAliasDefinitionBuilder<Value, ReferencedValue>.() -> Unit
): GraphAliasDefinition<Value, ReferencedValue> =
	graphAliasDefinition(
		valueClass = Value::class,
		referencedValueClass = ReferencedValue::class,
		configure = configure
	)


@RaptorDsl
fun <Value : Any, ReferencedValue : Any> graphAliasDefinition(
	valueClass: KClass<Value>,
	referencedValueClass: KClass<ReferencedValue>,
	configure: RaptorGraphAliasDefinitionBuilder<Value, ReferencedValue>.() -> Unit
): GraphAliasDefinition<Value, ReferencedValue> =
	RaptorGraphAliasDefinitionBuilder(
		referencedValueClass = referencedValueClass,
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Enum<Value>> graphEnumDefinition(
	@BuilderInference noinline configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
): GraphEnumDefinition<Value> =
	graphEnumDefinition(
		valueClass = Value::class,
		values = enumValues<Value>().toList(),
		configure = configure
	)


@RaptorDsl
fun <Value : Enum<Value>> graphEnumDefinition(
	valueClass: KClass<Value>,
	values: List<Value>, // FIXME validate
	configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
): GraphEnumDefinition<Value> =
	RaptorGraphEnumDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass,
		values = values
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphInputObjectDefinition(
	@BuilderInference noinline configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
): GraphInputObjectDefinition<Value> =
	graphInputObjectDefinition(valueClass = Value::class, configure = configure)


@RaptorDsl
fun <Value : Any> graphInputObjectDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
): GraphInputObjectDefinition<Value> =
	RaptorGraphInputObjectDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphInterfaceDefinition(
	@BuilderInference noinline configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceDefinition<Value> =
	graphInterfaceDefinition(valueClass = Value::class, configure = configure)


@RaptorDsl
fun <Value : Any> graphInterfaceDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceDefinition<Value> =
	RaptorGraphInterfaceDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphInterfaceExtensionDefinition(
	@BuilderInference noinline configure: RaptorGraphInterfaceExtensionDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceExtensionDefinition<Value> =
	graphInterfaceExtensionDefinition(valueClass = Value::class, configure = configure)


@RaptorDsl
fun <Value : Any> graphInterfaceExtensionDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphInterfaceExtensionDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceExtensionDefinition<Value> =
	RaptorGraphInterfaceExtensionDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphObjectDefinition(
	@BuilderInference noinline configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
): GraphObjectDefinition<Value> =
	graphObjectDefinition(valueClass = Value::class, configure = configure)


@RaptorDsl
fun <Value : Any> graphObjectDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
): GraphObjectDefinition<Value> =
	RaptorGraphObjectDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphObjectExtensionDefinition(
	@BuilderInference noinline configure: RaptorGraphObjectExtensionDefinitionBuilder<Value>.() -> Unit
): GraphObjectExtensionDefinition<Value> =
	graphObjectExtensionDefinition(valueClass = Value::class, configure = configure)


@RaptorDsl
fun <Value : Any> graphObjectExtensionDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphObjectExtensionDefinitionBuilder<Value>.() -> Unit
): GraphObjectExtensionDefinition<Value> =
	RaptorGraphObjectExtensionDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
inline fun <reified Value> graphOperationDefinition(
	name: String,
	type: RaptorGraphOperationType,
	@BuilderInference noinline configure: RaptorGraphOperationDefinitionBuilder<Value>.() -> Unit
): GraphOperationDefinition<Value> =
	graphOperationDefinition(name = name, type = type, valueType = typeOf<Value>(), configure = configure)


@RaptorDsl
fun <Value> graphOperationDefinition(
	name: String,
	type: RaptorGraphOperationType,
	valueType: KType,
	configure: RaptorGraphOperationDefinitionBuilder<Value>.() -> Unit
): GraphOperationDefinition<Value> =
	RaptorGraphOperationDefinitionBuilder<Value>(
		name = name,
		type = type,
		stackTrace = stackTrace(skipCount = 1),
		valueType = valueType
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphScalarDefinition(
	@BuilderInference noinline configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
): GraphScalarDefinition<Value> =
	graphScalarDefinition(valueClass = Value::class, configure = configure)


@RaptorDsl
fun <Value : Any> graphScalarDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
): GraphScalarDefinition<Value> =
	RaptorGraphScalarDefinitionBuilder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


internal fun checkGraphCompatibility(clazz: KClass<*>, root: KType = clazz.starProjectedType) {
	when (clazz) {
		Any::class, Nothing::class ->
			error("'$clazz' is not representable in GraphQL.\nFull type: $root")
	}

	if (clazz.typeParameters.isNotEmpty())
		error("Star-projected '${clazz.qualifiedName}' is not representable in GraphQL.\nFull type: $root")
}


internal fun checkGraphCompatibility(type: KType, root: KType = type) {
	when (val classifier = type.classifier) {
		Collection::class, List::class, Set::class -> checkGraphCompatibility(type.arguments.first(), root = root)
		is KClass<*> -> checkGraphCompatibility(classifier, root = root)
		is KTypeParameter -> error("Type parameter '$classifier' is not representable in GraphQL.\nFull type: $root")
		else -> error("Type classifier '$classifier' is not representable in GraphQL.\nFull type: $root")
	}
}


internal fun checkGraphCompatibility(typeProjection: KTypeProjection, root: KType) {
	when (val type = typeProjection.type) {
		null -> error("Star projections are not representable in GraphQL.\nFull type$root")
		else -> checkGraphCompatibility(type, root = root)
	}
}
