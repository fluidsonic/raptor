package io.fluidsonic.raptor


public interface RaptorComponentConfigurationEndScope<Component : RaptorComponent<Component>> : RaptorAssemblyCompletionScope {

	@RaptorDsl
	public val component: Component
}
