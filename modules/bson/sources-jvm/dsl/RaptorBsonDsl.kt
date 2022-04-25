package io.fluidsonic.raptor

import kotlin.reflect.*


public object RaptorBsonDsl


@RaptorDsl
@Suppress("unused")
public val RaptorGlobalDsl.bson: RaptorBsonDsl
	get() = RaptorBsonDsl


// FIXME Enable BuilderInference once fixed: https://youtrack.jetbrains.com/issue/KT-41595
@RaptorDsl
@Suppress("unused")
public inline fun <reified Value : Any> RaptorBsonDsl.definition(
	/* @BuilderInference */
	noinline configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
): RaptorBsonDefinition.ForValue<Value> =
	definition(valueClass = Value::class, configure = configure)


@RaptorDsl
@Suppress("unused")
public fun <Value : Any> RaptorBsonDsl.definition(
	valueClass: KClass<Value>,
	configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
): RaptorBsonDefinition.ForValue<Value> =
	RaptorBsonDefinitionBuilder(valueClass = valueClass).apply(configure).build()
