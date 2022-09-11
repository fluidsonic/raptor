package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


// TODO Make public if it's actually useful and after API was revisited.
@RaptorInternalApi
public interface RaptorDIGeneratingComponent : RaptorComponent2 {

	public companion object
}


@RaptorDsl
public val RaptorDIGeneratingComponent.di: RaptorDIComponent
	get() = componentRegistry2.oneOrRegister(RaptorDIFactoryComponent.Key, ::RaptorDIFactoryComponent)


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun RaptorConfigurationEndScope.diFactory(name: String, component: RaptorDIGeneratingComponent): RaptorDI.Factory =
	component.componentRegistry2.oneOrNull(RaptorDIFactoryComponent.Key)?.toFactory(name = name) ?: RaptorDI.Factory.empty
