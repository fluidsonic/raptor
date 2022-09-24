package io.fluidsonic.raptor


public interface RaptorComponentConfigurationEndScope<Component : RaptorComponent<Component>> : RaptorConfigurationEndScope {

	@RaptorDsl
	public val component: Component

	@RaptorDsl
	public val componentRegistry: RaptorComponentRegistry
}
