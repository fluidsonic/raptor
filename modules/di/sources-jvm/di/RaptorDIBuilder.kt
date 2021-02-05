package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
public interface RaptorDIBuilder {

	@RaptorDsl
	public fun provide(type: KType, provide: RaptorDI.() -> Any)
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIBuilder.provide(noinline provide: RaptorDI.() -> Dependency) {
	// withNullability(false) to work around https://youtrack.jetbrains.com/issue/KT-44726
	provide(typeOf<Dependency>().withNullability(false), provide = provide)
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIBuilder.provide(dependency: Dependency) {
	provide { dependency }
}
