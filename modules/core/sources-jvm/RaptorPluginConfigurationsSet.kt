package io.fluidsonic.raptor


public interface RaptorPluginConfigurationsSet {

	public fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> getOrNull(plugin: Plugin): Configuration?
}


public operator fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> RaptorPluginConfigurationsSet.get(
	plugin: Plugin,
): Configuration =
	getOrNull(plugin) ?: throw RaptorPluginNotInstalledException(plugin)
