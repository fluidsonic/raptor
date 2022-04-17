package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


public interface RaptorDIComponent : RaptorComponent.Typed<RaptorDIComponent> {

	@RaptorDsl
	public fun provide(type: KType, provide: RaptorDI.() -> Any?)
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorComponentSet<RaptorDIComponent>.provide(noinline provide: RaptorDI.() -> Dependency) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provide(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public fun RaptorComponentSet<RaptorDIComponent>.provide(type: KType, provide: RaptorDI.() -> Any) {
	configure {
		provide(type = type, provide = provide)
	}
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorComponentSet<RaptorDIComponent>.provide(dependency: Dependency) {
	provide { dependency }
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorComponentSet<RaptorDIComponent>.provideOptional(noinline provide: RaptorDI.() -> Dependency?) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-45066
	provideOptional(typeOf<Dependency>().withNullability(false), provide = provide)
}


@RaptorDsl
public fun RaptorComponentSet<RaptorDIComponent>.provideOptional(type: KType, provide: RaptorDI.() -> Any?) {
	configure {
		provide(type = type, provide = provide)
	}
}


@RaptorDsl
public inline fun <reified Dependency : Any> RaptorComponentSet<RaptorDIComponent>.provideOptional(dependency: Dependency?) {
	provideOptional { dependency }
}
