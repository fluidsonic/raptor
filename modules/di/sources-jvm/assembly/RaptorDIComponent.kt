@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.reflect.*
import kotlin.reflect.full.*


public interface RaptorDIComponent<Component : RaptorDIComponent<Component>> : RaptorComponent<Component> {

	@RaptorDsl
	public fun provide(type: KType, provide: RaptorDI.() -> Any?)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(type: KType, provide: RaptorDI.() -> Any?) {
	each {
		provide(type = type, provide = provide)
	}
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(
	noinline provide: RaptorDI.() -> @NoInfer Dependency,
) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provide(typeOf<Dependency>().withNullability(false), provide = provide)
}


@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-54478/NoInfer-causes-CONFLICTINGOVERLOADS
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(dependency: @NoInfer Dependency) {
	provide<Dependency> { dependency }
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(
	noinline provide: RaptorDI.() -> @NoInfer Dependency?,
) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provideOptional(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(type: KType, provide: RaptorDI.() -> Any?) {
	provide(type = type, provide = provide)
}


@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-54478/NoInfer-causes-CONFLICTINGOVERLOADS
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(dependency: @NoInfer Dependency?) {
	provideOptional<Dependency> { dependency }
}
