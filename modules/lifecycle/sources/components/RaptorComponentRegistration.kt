package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorComponentRegistration<out Component : RaptorComponent> {

	val component: Component
	val containingRegistry: RaptorComponentRegistry
	val registry: RaptorComponentRegistry


	interface Mutable<out Component : RaptorComponent> : RaptorComponentRegistration<Component> {

		override val containingRegistry: RaptorComponentRegistry.Mutable
		override val registry: RaptorComponentRegistry.Mutable
	}
}
