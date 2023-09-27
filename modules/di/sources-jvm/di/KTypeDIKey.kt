@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.di

import kotlin.internal.*
import kotlin.reflect.*


internal class KTypeDIKey<Value>(
	val type: KType,
) : RaptorDIKey<Value> {

	override fun equals(other: Any?): Boolean =
		this === other || (other is KTypeDIKey<*> && type == other.type)


	override fun hashCode(): Int =
		type.hashCode()


	override val isOptional: Boolean
		get() = type.isMarkedNullable


	override fun toString(): String =
		type.toString()
}


public fun <Value> RaptorDIKey(type: KType): RaptorDIKey<@NoInfer Value> =
	KTypeDIKey(type = type)


public inline fun <reified Value> RaptorDIKey(): RaptorDIKey<@NoInfer Value> =
	RaptorDIKey<Value>(typeOf<Value>())
