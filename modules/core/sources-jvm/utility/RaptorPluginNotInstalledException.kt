package io.fluidsonic.raptor


public class RaptorPluginNotInstalledException(
	public val plugin: RaptorPlugin,
) : RuntimeException("Plugin ${plugin::class.qualifiedName ?: plugin.toString()} is not installed.")
