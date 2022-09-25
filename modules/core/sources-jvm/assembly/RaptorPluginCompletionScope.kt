package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorPluginCompletionScope : RaptorAssemblyCompletionScope {

	@RaptorDsl
	public fun <Plugin : RaptorPluginWithConfiguration<*>> configure(plugin: Plugin, action: RaptorPluginScope<Plugin>.() -> Unit = {})

	@RaptorDsl
	public fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> require(
		plugin: Plugin,
		action: (configuration: Configuration) -> Unit = {},
	)
}
