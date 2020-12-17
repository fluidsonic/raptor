package io.fluidsonic.raptor

import kotlin.internal.*
import kotlin.properties.*
import kotlin.reflect.*


public interface RaptorDI {

	@RaptorDsl
	public fun get(type: KType): Any?


	@RaptorDsl
	public operator fun <Value> invoke(factory: RaptorDI.() -> Value): Lazy<Value>


	override fun toString(): String
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Value> RaptorDI.get(): Value =
	get(typeOf<Value>()) as Value


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline operator fun <reified Value> RaptorDI.invoke(): PropertyDelegateProvider<Any?, Lazy<Value>> =
	object : PropertyDelegateProvider<Any?, Lazy<Value>> {

		private val type = typeOf<Value>()


		override fun provideDelegate(thisRef: Any?, property: KProperty<*>): Lazy<Value> =
			lazy { get(type) as Value }
	}


@RaptorDsl
public val RaptorScope.di: RaptorDI
	get() = context.properties[DIRaptorPropertyKey]
		?: error("You must install ${RaptorDIFeature::class.simpleName} for enabling dependency injection functionality.")


@LowPriorityInOverloadResolution
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public inline fun <reified Value> RaptorScope.di(): PropertyDelegateProvider<Any?, Lazy<Value>> =
	di.invoke()


@LowPriorityInOverloadResolution
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public inline fun <reified Value> RaptorScope.di(
	noinline factory: RaptorDI.() -> Value,
): Lazy<Value> =
	di.invoke(factory)
