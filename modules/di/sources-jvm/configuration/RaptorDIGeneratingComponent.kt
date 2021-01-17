package io.fluidsonic.raptor


// TODO Make public if it's actually useful and after API was revisited.
@InternalRaptorApi
public interface RaptorDIGeneratingComponent : RaptorComponent {

	public companion object
}


@RaptorDsl
public fun RaptorComponentSet<RaptorDIGeneratingComponent>.di(configuration: RaptorDIBuilder.() -> Unit) {
	configure {
		componentRegistry.oneOrRegister(DIFactoryRaptorComponent.Key) { DIFactoryRaptorComponent() }.configure {
			builder.apply(configuration)
		}
	}
}


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("unused")
public fun RaptorConfigurationEndScope.diFactory(name: String, component: RaptorDIGeneratingComponent): RaptorDI.Factory =
	component.componentRegistry.oneOrNull(DIFactoryRaptorComponent.Key)?.toFactory(name = name)
		?: DefaultRaptorDI.Factory(modules = emptyList())
