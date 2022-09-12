package io.fluidsonic.raptor


internal class RaptorComponentTagRegistry2<Component : RaptorTaggableComponent2>(
	private val component: Component,
) {

	private var pendingConfigurations: List<RaptorTaggableComponentConfiguration2<Component>> = emptyList()

	var tags: Set<Any> = emptySet()
		private set


	fun addConfiguration(configuration: RaptorTaggableComponentConfiguration2<Component>) {
		when {
			configuration.tagsToMatch.any(tags::contains) -> configuration.configure(component)
			else -> pendingConfigurations = pendingConfigurations + configuration
		}
	}


	fun addTags(newTags: Set<Any>) {
		if (newTags.all(tags::contains))
			return

		tags = tags + newTags

		val matchingConfigurations = mutableListOf<RaptorTaggableComponentConfiguration2<Component>>()
		val stillPendingConfigurations = mutableListOf<RaptorTaggableComponentConfiguration2<Component>>()

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
