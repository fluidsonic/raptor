package io.fluidsonic.raptor


internal class RaptorComponentTagRegistry<Component : RaptorTaggableComponent<Component>>(
	private val component: Component,
) {

	private var pendingConfigurations: List<RaptorTaggableComponentConfiguration<Component>> = emptyList()

	var tags: Set<Any> = emptySet()
		private set


	fun addConfiguration(configuration: RaptorTaggableComponentConfiguration<Component>) {
		when {
			configuration.tagsToMatch.any(tags::contains) -> configuration.configure(component)
			else -> pendingConfigurations = pendingConfigurations + configuration
		}
	}


	fun addTags(newTags: Set<Any>) {
		if (newTags.all(tags::contains))
			return

		tags = tags + newTags

		val matchingConfigurations = mutableListOf<RaptorTaggableComponentConfiguration<Component>>()
		val stillPendingConfigurations = mutableListOf<RaptorTaggableComponentConfiguration<Component>>()

		for (configuration in pendingConfigurations)
			when {
				configuration.tagsToMatch.any(tags::contains) -> matchingConfigurations += configuration
				else -> stillPendingConfigurations += configuration
			}

		pendingConfigurations = stillPendingConfigurations

		for (configuration in matchingConfigurations)
			configuration.configure(component)
	}
}


@Suppress("UNCHECKED_CAST")
internal fun <Component : RaptorTaggableComponent<out Component>> Component.tagRegistry(): RaptorComponentTagRegistry<out Component>? =
	extensions[Keys.tagRegistryComponentExtension] as RaptorComponentTagRegistry<out Component>?


internal fun <Component : RaptorTaggableComponent<Component>> Component.tagRegistryOrCreate(): RaptorComponentTagRegistry<out Component> =
	tagRegistry() ?: RaptorComponentTagRegistry(component = this).also { extensions[Keys.tagRegistryComponentExtension] = it }
