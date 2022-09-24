package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.reflect.*
import kotlin.reflect.full.*


public interface RaptorDIComponent<Component : RaptorDIComponent<Component>> : RaptorComponent<Component> {

	@RaptorDsl
	public fun provide(type: KType, provide: RaptorDI.() -> Any?)
}


@RaptorDsl
public operator fun <Component : RaptorDIComponent<Component>> Component.invoke(configure: Component.() -> Unit) {
	configure()
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIComponent<*>.provide(noinline provide: RaptorDI.() -> Dependency) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provide(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIComponent<*>.provide(dependency: Dependency) {
	provide { dependency }
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIComponent<*>.provideOptional(noinline provide: RaptorDI.() -> Dependency?) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provideOptional(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public fun RaptorDIComponent<*>.provideOptional(type: KType, provide: RaptorDI.() -> Any?) {
	provide(type = type, provide = provide)
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIComponent<*>.provideOptional(dependency: Dependency?) {
	provideOptional { dependency }
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(type: KType, provide: RaptorDI.() -> Any?) {
	each {
		provide(type = type, provide = provide)
	}
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(noinline provide: RaptorDI.() -> Dependency) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provide(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provide(dependency: Dependency) {
	provide { dependency }
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(noinline provide: RaptorDI.() -> Dependency?) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provideOptional(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(type: KType, provide: RaptorDI.() -> Any?) {
	provide(type = type, provide = provide)
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorAssemblyQuery<RaptorDIComponent<*>>.provideOptional(dependency: Dependency?) {
	provideOptional { dependency }
}
