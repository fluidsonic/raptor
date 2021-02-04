package io.fluidsonic.raptor


// TODO Make public if it's actually useful and after API was revisited.
@RaptorInternalApi
public interface RaptorDIGeneratingComponent : RaptorComponent {

	public companion object
}


@RaptorDsl
public val RaptorComponentSet<RaptorDIGeneratingComponent>.di: RaptorComponentSet<RaptorDIComponent>
	get() = withComponentAuthoring {
		map { componentRegistry.oneOrRegister(RaptorDIFactoryComponent.Key, ::RaptorDIFactoryComponent) }
	}


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("unused")
public fun RaptorConfigurationEndScope.diFactory(name: String, component: RaptorDIGeneratingComponent): RaptorDI.Factory =
	component.componentRegistry.oneOrNull(RaptorDIFactoryComponent.Key)?.toFactory(name = name)
		?: DefaultRaptorDI.Factory(modules = emptyList())
