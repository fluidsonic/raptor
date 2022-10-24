@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.di

import kotlin.internal.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class KTypeDIKey<Value : Any>(
	val type: KType,
) : RaptorDIKey<Value> {

	override fun equals(other: Any?): Boolean =
		this === other || (other is KTypeDIKey<*> && type == other.type)


	override fun hashCode(): Int =
		type.hashCode()


	override fun toString(): String =
		type.toString()
}


@Suppress("FunctionName")
public fun <Value : Any> RaptorDIKey(type: KType): RaptorDIKey<@NoInfer Value> =
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	KTypeDIKey(type = type.withNullability(false))


@Suppress("FunctionName")
public inline fun <reified Value : Any> RaptorDIKey(): RaptorDIKey<@NoInfer Value> =
	RaptorDIKey<Value>(typeOf<Value>())
