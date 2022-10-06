package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorPluginCompletionScope : RaptorAssemblyCompletionScope {

	@RaptorDsl
	public fun completeComponents()

	@RaptorDsl
	public fun <Plugin : RaptorPluginWithConfiguration<*>> configure(plugin: Plugin, action: RaptorPluginScope<Plugin>.() -> Unit = {})
}
