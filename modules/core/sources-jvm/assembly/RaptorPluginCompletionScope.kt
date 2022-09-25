package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorPluginCompletionScope : RaptorAssemblyCompletionScope {

	@RaptorDsl
	public fun <Plugin: RaptorPlugin> complete(plugin: Plugin, action: RaptorPluginScope<Plugin>.() -> Unit = {})
}
