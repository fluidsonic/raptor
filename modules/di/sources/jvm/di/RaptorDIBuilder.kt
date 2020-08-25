package io.fluidsonic.raptor

import kotlin.reflect.*


@RaptorDsl
public interface RaptorDIBuilder {

	@RaptorDsl
	public fun provide(type: KType, provide: RaptorDI.() -> Any)
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIBuilder.provide(noinline provide: RaptorDI.() -> Dependency) {
	provide(typeOf<Dependency>(), provide = provide)
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Dependency : Any> RaptorDIBuilder.provide(dependency: Dependency) {
	provide { dependency }
}
