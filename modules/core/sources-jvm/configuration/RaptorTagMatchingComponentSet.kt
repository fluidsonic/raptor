package io.fluidsonic.raptor


internal class RaptorTagMatchingComponentSet<out Component : RaptorTaggableComponent>(
	private val source: RaptorComponentSet<Component>,
	private val tagsToMatch: Set<Any>,
) : RaptorComponentSet<Component> {

	override fun configure(action: Component.() -> Unit) {
		val configuration = RaptorTaggableComponentConfiguration(tagsToMatch = tagsToMatch, action = action)

		source.configure {
			tagRegistry.addConfiguration(configuration)
		}
	}
}
