@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.di

import kotlin.internal.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class LabeledDIKey<Value>(
	val label: String,
	val type: KType,
) : RaptorDIKey<Value> {

	override val isOptional: Boolean
		get() = type.isMarkedNullable


	override fun notOptional(): LabeledDIKey<Value> =
		when (type.isMarkedNullable) {
			true -> LabeledDIKey(label = label, type = type.withNullability(false))
			false -> this
		}


	override fun toString() =
		"$type ($label)"
}


public fun <Value> RaptorDIKey(label: String, type: KType): RaptorDIKey<@NoInfer Value> =
	LabeledDIKey(label = label, type = type)


public inline fun <reified Value> RaptorDIKey(label: String): RaptorDIKey<@NoInfer Value> =
	RaptorDIKey<Value>(label = label, type = typeOf<Value>())
