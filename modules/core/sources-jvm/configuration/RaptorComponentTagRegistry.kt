package io.fluidsonic.raptor


internal class RaptorComponentTagRegistry<Component : RaptorTaggableComponent>(
	private val component: Component,
) {

	private var pendingConfigurations: List<RaptorTaggableComponentConfiguration<Component>> = emptyList()

	var tags: Set<Any> = emptySet()
		private set


	fun addConfiguration(configuration: RaptorTaggableComponentConfiguration<Component>) {
		if (configuration.tagsToMatch.all(tags::contains))
			configuration.action(component)
		else
			pendingConfigurations = pendingConfigurations + configuration
	}


	fun addTags(newTags: Set<Any>) {
		if (newTags.all(tags::contains))
			return

		tags = tags + newTags

		val matchingConfigurations = mutableListOf<RaptorTaggableComponentConfiguration<Component>>()
		val stillPendingConfigurations = mutableListOf<RaptorTaggableComponentConfiguration<Component>>()

		for (configuration in pendingConfigurations)
			if (configuration.tagsToMatch.all(tags::contains))
				matchingConfigurations += configuration
			else
				stillPendingConfigurations += configuration

		pendingConfigurations = stillPendingConfigurations

		for (configuration in matchingConfigurations)
			configuration.action(component)
	}
}
