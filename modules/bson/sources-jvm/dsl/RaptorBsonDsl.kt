@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.reflect.*


public object RaptorBsonDsl


@RaptorDsl
@Suppress("UnusedReceiverParameter")
public val RaptorGlobalDsl.bson: RaptorBsonDsl
	get() = RaptorBsonDsl


// https://youtrack.jetbrains.com/issue/KT-54477/NoInfer-doesnt-work-for-builders
@RaptorDsl
public inline fun <reified Value : Any> RaptorBsonDsl.definition(
	noinline configure: RaptorBsonDefinitionBuilder<@NoInfer Value>.() -> Unit,
): RaptorBsonDefinition.ForValue<@NoInfer Value> =
	definition(valueClass = Value::class, configure = configure)


@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun <Value : Any> RaptorBsonDsl.definition(
	valueClass: KClass<Value>,
	configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
): RaptorBsonDefinition.ForValue<Value> =
	RaptorBsonDefinitionBuilder(valueClass = valueClass).apply(configure).build()
