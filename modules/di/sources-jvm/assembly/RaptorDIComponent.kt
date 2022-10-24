@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.reflect.*


public interface RaptorDIComponent<Component : RaptorDIComponent<Component>> : RaptorComponent<Component> {

	@RaptorDsl
	public fun <Value : Any> provide(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value)

	@RaptorDsl
	public fun <Value : Any> provideOptional(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value?)
}


@RaptorDsl
public fun <Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value) {
	each {
		provide(key = key, provide = provide)
	}
}


@RaptorDsl
public fun <Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(type: KType, provide: RaptorDI.() -> @NoInfer Value) {
	provide(key = RaptorDIKey<Value>(type), provide = provide)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(
	noinline provide: RaptorDI.() -> @NoInfer Value,
) {
	provide<Value>(type = typeOf<Value>(), provide = provide)
}


@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-54478/NoInfer-causes-CONFLICTINGOVERLOADS
@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(value: @NoInfer Value) {
	provide<Value> { value }
}


@RaptorDsl
public fun <Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value?) {
	each {
		provideOptional(key = key, provide = provide)
	}
}


@RaptorDsl
public fun <Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(type: KType, provide: RaptorDI.() -> @NoInfer Value?) {
	provideOptional(key = RaptorDIKey<Value>(type), provide = provide)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(
	noinline provide: RaptorDI.() -> @NoInfer Value?,
) {
	provideOptional<Value>(type = typeOf<Value>(), provide = provide)
}


@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-54478/NoInfer-causes-CONFLICTINGOVERLOADS
@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(dependency: @NoInfer Value?) {
	provideOptional<Value> { dependency }
}
