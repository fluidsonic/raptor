package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*

// TODO Allow nested definitions in structured definitions & reuse in RaptorGraphOperationBuilder.
// TODO Can also improve automatic name generation.


@RaptorDsl
public inline fun <reified Type : Any, reified ReferencedType : Any> graphAliasDefinition(
	@BuilderInference noinline configure: RaptorAliasGraphDefinitionBuilder<Type, ReferencedType>.() -> Unit,
): RaptorGraphDefinition =
	graphAliasDefinition(
		type = typeOf<Type>(),
		referencedType = typeOf<ReferencedType>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any, ReferencedType : Any> graphAliasDefinition(
	type: KType,
	referencedType: KType,
	configure: RaptorAliasGraphDefinitionBuilder<Type, ReferencedType>.() -> Unit,
): RaptorGraphDefinition =
	RaptorAliasGraphDefinitionBuilder<Type, ReferencedType>(
		isId = false,
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		referencedKotlinType = KotlinType.of(
			type = referencedType,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		stackTrace = stackTrace(skipCount = 1),
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Enum<Type>> graphEnumDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorEnumGraphDefinitionBuilder<Type>.() -> Unit = {},
): RaptorGraphDefinition =
	graphEnumDefinition(
		name = name,
		type = typeOf<Type>(),
		values = enumValues<Type>().toList(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Enum<Type>> graphEnumDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	values: List<Type>, // TODO validate
	configure: RaptorEnumGraphDefinitionBuilder<Type>.() -> Unit = {},
): RaptorGraphDefinition =
	RaptorEnumGraphDefinitionBuilder(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = true
		),
		name = RaptorGraphDefinition.resolveName(name, type = type),
		stackTrace = stackTrace(skipCount = 1),
		values = values
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Any> graphIdAliasDefinition(
	@BuilderInference noinline configure: RaptorAliasGraphDefinitionBuilder<Type, String>.() -> Unit,
): RaptorGraphDefinition =
	graphIdAliasDefinition(
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphIdAliasDefinition(
	type: KType,
	configure: RaptorAliasGraphDefinitionBuilder<Type, String>.() -> Unit,
): RaptorGraphDefinition =
	RaptorAliasGraphDefinitionBuilder<Type, String>(
		isId = true,
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		referencedKotlinType = KotlinType.of(
			type = typeOf<String>(),
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Any> graphInputObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorInputObjectGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	graphInputObjectDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphInputObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorInputObjectGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	RaptorInputObjectGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		name = RaptorGraphDefinition.resolveName(name, type = type),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Any> graphInterfaceDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	noinline configure: RaptorInterfaceGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	graphInterfaceDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphInterfaceDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorInterfaceGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	RaptorInterfaceGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		name = RaptorGraphDefinition.resolveName(name, type = type),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Any> graphInterfaceExtensionDefinition(
	@BuilderInference noinline configure: RaptorInterfaceExtensionGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	graphInterfaceExtensionDefinition(
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphInterfaceExtensionDefinition(
	type: KType,
	configure: RaptorInterfaceExtensionGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	RaptorInterfaceExtensionGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


// TODO put all dsl behind an object for grouping & reuse in nested{} blocks
@RaptorDsl
public inline fun <reified Type : Any> graphObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorObjectGraphDefinitionBuilder<Type>.() -> Unit = {},
): RaptorGraphDefinition =
	graphObjectDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorObjectGraphDefinitionBuilder<Type>.() -> Unit = {},
): RaptorGraphDefinition =
	RaptorObjectGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		name = RaptorGraphDefinition.resolveName(name, type = type),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Any> graphObjectExtensionDefinition(
	@BuilderInference noinline configure: RaptorObjectExtensionGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	graphObjectExtensionDefinition(
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphObjectExtensionDefinition(
	type: KType,
	configure: RaptorObjectExtensionGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	RaptorObjectExtensionGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Value> graphOperationDefinition(
	name: String,
	operationType: RaptorGraphOperationType,
	@BuilderInference noinline configure: RaptorGraphOperationDefinitionBuilder<Value>.() -> Unit,
): RaptorGraphDefinition =
	graphOperationDefinition(
		name = name,
		operationType = operationType,
		type = typeOf<Value>(),
		configure = configure
	)


@RaptorDsl
public fun <Value> graphOperationDefinition(
	name: String,
	type: KType,
	operationType: RaptorGraphOperationType,
	configure: RaptorGraphOperationDefinitionBuilder<Value>.() -> Unit,
): RaptorGraphDefinition =
	RaptorGraphOperationDefinitionBuilder<Value>(
		additionalDefinitions = emptyList(),
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = true,
			allowedVariance = KVariance.OUT,
			requireSpecialization = true
		),
		name = name,
		operationType = operationType,
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Value : Any> graphScalarDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorScalarGraphDefinitionBuilder<Value>.() -> Unit,
): RaptorGraphDefinition =
	graphScalarDefinition(
		name = name,
		type = typeOf<Value>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphScalarDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorScalarGraphDefinitionBuilder<Type>.() -> Unit,
): RaptorGraphDefinition =
	RaptorScalarGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		name = RaptorGraphDefinition.resolveName(name, type = type),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Type : Any> graphUnionDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorUnionGraphDefinitionBuilder<Type>.() -> Unit = {},
): RaptorGraphDefinition =
	graphUnionDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphUnionDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorUnionGraphDefinitionBuilder<Type>.() -> Unit = {},
): RaptorGraphDefinition =
	RaptorUnionGraphDefinitionBuilder<Type>(
		kotlinType = KotlinType.of(
			type = type,
			containingType = null,
			allowMaybe = false,
			allowNull = false,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = false
		),
		name = RaptorGraphDefinition.resolveName(name, type = type),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()
