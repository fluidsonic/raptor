package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


public interface RaptorDIComponent : RaptorComponent.Typed<RaptorDIComponent> {

	@RaptorDsl
	public fun provide(type: KType, provide: RaptorDI.() -> Any)
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorComponentSet<RaptorDIComponent>.provide(noinline provide: RaptorDI.() -> Dependency) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-44726
	provide(typeOf<Dependency>().withNullability(false), provide = provide)
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public fun RaptorComponentSet<RaptorDIComponent>.provide(type: KType, provide: RaptorDI.() -> Any) {
	configure {
		provide(type = type, provide = provide)
	}
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorComponentSet<RaptorDIComponent>.provide(dependency: Dependency) {
	provide { dependency }
}
