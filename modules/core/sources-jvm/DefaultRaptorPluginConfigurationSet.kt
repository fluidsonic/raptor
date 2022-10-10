package io.fluidsonic.raptor


internal class DefaultRaptorPluginConfigurationSet(
	private val values: Map<RaptorPluginWithConfiguration<*>, Any>,
) : RaptorPluginConfigurationsSet {

	@Suppress("UNCHECKED_CAST")
	override fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> getOrNull(plugin: Plugin): Configuration? =
		values[plugin] as Configuration?


	override fun toString() =
		when (values.isEmpty()) {
			true -> "<empty>"
			false -> values
				.map { (plugin, configuration) ->
					plugin.toString() to when (configuration) {
						Unit -> "<none>"
						else -> configuration.toString()
					}
				}
				.sortedBy { it.first }
				.joinToString("\n") { (plugin, configuration) -> "$plugin -> $configuration" }
		}
}
