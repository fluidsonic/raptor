package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*

// FIXME Allow nested definitions in structured definitions & reuse in RaptorGraphOperationBuilder.
// FIXME Can also improve automatic name generation.


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
		isId = false,
		referencedValueClass = referencedValueClass,
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Enum<Value>> graphEnumDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
): GraphEnumDefinition<Value> =
	graphEnumDefinition(
		name = name,
		valueClass = Value::class,
		values = enumValues<Value>().toList(),
		configure = configure
	)


@RaptorDsl
fun <Value : Enum<Value>> graphEnumDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	valueClass: KClass<Value>,
	values: List<Value>, // FIXME validate
	configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
): GraphEnumDefinition<Value> =
	RaptorGraphEnumDefinitionBuilder(
		name = RaptorGraphDefinition.resolveName(name, valueClass = valueClass),
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass,
		values = values
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphIdAliasDefinition(
	@BuilderInference noinline configure: RaptorGraphAliasDefinitionBuilder<Value, String>.() -> Unit
): GraphAliasDefinition<Value, String> =
	graphIdAliasDefinition(
		valueClass = Value::class,
		configure = configure
	)


@RaptorDsl
fun <Value : Any> graphIdAliasDefinition(
	valueClass: KClass<Value>,
	configure: RaptorGraphAliasDefinitionBuilder<Value, String>.() -> Unit
): GraphAliasDefinition<Value, String> =
	RaptorGraphAliasDefinitionBuilder(
		isId = true,
		referencedValueClass = String::class,
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphInputObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
): GraphInputObjectDefinition<Value> =
	graphInputObjectDefinition(
		name = name,
		valueClass = Value::class,
		configure = configure
	)


@RaptorDsl
fun <Value : Any> graphInputObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	valueClass: KClass<Value>,
	configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
): GraphInputObjectDefinition<Value> =
	RaptorGraphInputObjectDefinitionBuilder(
		name = RaptorGraphDefinition.resolveName(name, valueClass = valueClass),
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphInterfaceDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceDefinition<Value> =
	graphInterfaceDefinition(
		name = name,
		valueClass = Value::class,
		configure = configure
	)


@RaptorDsl
fun <Value : Any> graphInterfaceDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	valueClass: KClass<Value>,
	configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceDefinition<Value> =
	RaptorGraphInterfaceDefinitionBuilder(
		name = RaptorGraphDefinition.resolveName(name, valueClass = valueClass),
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphInterfaceExtensionDefinition(
	@BuilderInference noinline configure: RaptorGraphInterfaceExtensionDefinitionBuilder<Value>.() -> Unit
): GraphInterfaceExtensionDefinition<Value> =
	graphInterfaceExtensionDefinition(
		valueClass = Value::class,
		configure = configure
	)


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


// FIXME put all dsl behind an object for grouping & reuse in nested{} blocks
@RaptorDsl
inline fun <reified Value : Any> graphObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
): GraphObjectDefinition<Value> =
	graphObjectDefinition(
		name = name,
		valueClass = Value::class,
		configure = configure
	)


@RaptorDsl
fun <Value : Any> graphObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	valueClass: KClass<Value>,
	configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
): GraphObjectDefinition<Value> =
	RaptorGraphObjectDefinitionBuilder(
		name = RaptorGraphDefinition.resolveName(name, valueClass = valueClass),
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphObjectExtensionDefinition(
	@BuilderInference noinline configure: RaptorGraphObjectExtensionDefinitionBuilder<Value>.() -> Unit
): GraphObjectExtensionDefinition<Value> =
	graphObjectExtensionDefinition(
		valueClass = Value::class,
		configure = configure
	)


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
	graphOperationDefinition(
		name = name,
		type = type,
		valueType = typeOf<Value>(),
		configure = configure
	)


@RaptorDsl
fun <Value> graphOperationDefinition(
	name: String,
	type: RaptorGraphOperationType,
	valueType: KType,
	configure: RaptorGraphOperationDefinitionBuilder<Value>.() -> Unit
): GraphOperationDefinition<Value> =
	RaptorGraphOperationDefinitionBuilder<Value>(
		additionalDefinitions = emptyList(),
		name = name,
		type = type,
		stackTrace = stackTrace(skipCount = 1),
		valueType = valueType
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Value : Any> graphScalarDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
): GraphScalarDefinition<Value> =
	graphScalarDefinition(
		name = name,
		valueClass = Value::class,
		configure = configure
	)


@RaptorDsl
fun <Value : Any> graphScalarDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	valueClass: KClass<Value>,
	configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
): GraphScalarDefinition<Value> =
	RaptorGraphScalarDefinitionBuilder(
		name = RaptorGraphDefinition.resolveName(name, valueClass = valueClass),
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


internal fun checkGraphCompatibility(clazz: KClass<*>) {
	check(isGraphRepresentable(clazz)) {
		error("Kotlin type is not representable in GraphQL: $clazz")
	}
}


internal fun checkGraphCompatibility(type: KType, isMaybeAllowed: Boolean = false) {
	val isRepresentable = if (isMaybeAllowed && type.classifier == Maybe::class)
		isGraphRepresentable(type.arguments.first())
	else
		isGraphRepresentable(type)

	check(isRepresentable) {
		error("Kotlin type is not representable in GraphQL: $type")
	}
}


private fun isGraphRepresentable(clazz: KClass<*>): Boolean =
	when (clazz) {
		Any::class, Nothing::class -> false
		else -> clazz.typeParameters.isEmpty()
	}


private fun isGraphRepresentable(type: KType): Boolean =
	when (val classifier = type.classifier) {
		Collection::class, List::class, Set::class -> isGraphRepresentable(type.arguments.first())
		is KClass<*> -> isGraphRepresentable(classifier)
		else -> false
	}


private fun isGraphRepresentable(typeProjection: KTypeProjection): Boolean =
	when (val type = typeProjection.type) {
		null -> false
		else -> isGraphRepresentable(type)
	}
