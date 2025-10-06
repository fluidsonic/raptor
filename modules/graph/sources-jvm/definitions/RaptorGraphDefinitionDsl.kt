@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.stdlib.*
import kotlin.internal.*
import kotlin.reflect.*

// TODO Allow nested definitions in structured definitions & reuse in RaptorGraphOperationBuilder.
// TODO Can also improve automatic name generation.


@RaptorDsl
public inline fun <reified Type : Any, reified ReferencedType : Any> graphAliasDefinition(
	noinline configure: RaptorAliasGraphDefinitionBuilder<@NoInfer Type, @NoInfer ReferencedType>.() -> Unit,
): RaptorGraphDefinition =
	graphAliasDefinition<Type, ReferencedType>(
		type = typeOf<Type>(),
		referencedType = typeOf<ReferencedType>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any, ReferencedType : Any> graphAliasDefinition(
	type: KType,
	referencedType: KType,
	configure: RaptorAliasGraphDefinitionBuilder<@NoInfer Type, @NoInfer ReferencedType>.() -> Unit,
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
	noinline configure: RaptorEnumGraphDefinitionBuilder<@NoInfer Type>.() -> Unit = {},
): RaptorGraphDefinition =
	graphEnumDefinition<Type>(
		name = name,
		type = typeOf<Type>(),
		values = enumValues<Type>().toList(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Enum<Type>> graphEnumDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	values: List<@NoInfer Type>, // TODO validate
	configure: RaptorEnumGraphDefinitionBuilder<@NoInfer Type>.() -> Unit = {},
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
	noinline configure: RaptorAliasGraphDefinitionBuilder<@NoInfer Type, String>.() -> Unit,
): RaptorGraphDefinition =
	graphIdAliasDefinition<Type>(
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphIdAliasDefinition(
	type: KType,
	configure: RaptorAliasGraphDefinitionBuilder<@NoInfer Type, String>.() -> Unit,
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
	noinline configure: RaptorInputObjectGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
): RaptorGraphDefinition =
	graphInputObjectDefinition<Type>(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphInputObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorInputObjectGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
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
	noinline configure: RaptorInterfaceGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
): RaptorGraphDefinition =
	graphInterfaceDefinition<Type>(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphInterfaceDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorInterfaceGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
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
	noinline configure: RaptorInterfaceExtensionGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
): RaptorGraphDefinition =
	graphInterfaceExtensionDefinition<Type>(
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphInterfaceExtensionDefinition(
	type: KType,
	configure: RaptorInterfaceExtensionGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
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
	noinline configure: RaptorObjectGraphDefinitionBuilder<@NoInfer Type>.() -> Unit = {},
): RaptorGraphDefinition =
	graphObjectDefinition<Type>(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphObjectDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorObjectGraphDefinitionBuilder<@NoInfer Type>.() -> Unit = {},
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
	noinline configure: RaptorObjectExtensionGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
): RaptorGraphDefinition =
	graphObjectExtensionDefinition<Type>(
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphObjectExtensionDefinition(
	type: KType,
	configure: RaptorObjectExtensionGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
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
	noinline configure: RaptorGraphOperationDefinitionBuilder<@NoInfer Value>.() -> Unit,
): RaptorGraphDefinition =
	graphOperationDefinition<Value>(
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
	configure: RaptorGraphOperationDefinitionBuilder<@NoInfer Value>.() -> Unit,
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
public inline fun <reified Type : Any> graphScalarDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	noinline configure: RaptorScalarGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
): RaptorGraphDefinition =
	graphScalarDefinition<Type>(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphScalarDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorScalarGraphDefinitionBuilder<@NoInfer Type>.() -> Unit,
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
	noinline configure: RaptorUnionGraphDefinitionBuilder<@NoInfer Type>.() -> Unit = {},
): RaptorGraphDefinition =
	graphUnionDefinition<Type>(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)


@RaptorDsl
public fun <Type : Any> graphUnionDefinition(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorUnionGraphDefinitionBuilder<@NoInfer Type>.() -> Unit = {},
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
