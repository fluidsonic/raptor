package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*

private val factoryKey = RaptorComponentKey<RaptorDIFactoryComponent>("DI factory")


// TODO Make public if it's actually useful and after API was revisited.
@RaptorInternalApi
public interface RaptorDIBoundary<Component : RaptorDIBoundary<Component>> : RaptorComponent<Component> {

	public companion object
}


@RaptorDsl
public val RaptorDIBoundary<*>.di: RaptorDIComponent<*>
	get() = componentRegistry.oneOrRegister(factoryKey, ::RaptorDIFactoryComponent)


// TODO Throw if plugin isn't installed.
@RaptorDsl
public fun RaptorComponentConfigurationEndScope<out RaptorDIBoundary<*>>.diFactory(name: String): RaptorDI.Factory =
	componentRegistry.oneOrNull(factoryKey)?.toFactory(name = name) ?: RaptorDI.Factory.empty
