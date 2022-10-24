@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.di

import kotlin.internal.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class LabeledDIKey<Value : Any>(
	val label: String,
	val type: KType,
) : RaptorDIKey<Value> {

	override fun toString() =
		"$type ($label)"
}


@Suppress("FunctionName")
public fun <Value : Any> RaptorDIKey(label: String, type: KType): RaptorDIKey<@NoInfer Value> =
	LabeledDIKey(label = label, type = type.withNullability(false))


@Suppress("FunctionName")
public inline fun <reified Value : Any> RaptorDIKey(label: String): RaptorDIKey<@NoInfer Value> =
	RaptorDIKey<Value>(label = label, type = typeOf<Value>())
