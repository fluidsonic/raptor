package io.fluidsonic.raptor


public interface RaptorPluginConfigurationsSet {

	public operator fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> get(plugin: Plugin): Configuration
}
