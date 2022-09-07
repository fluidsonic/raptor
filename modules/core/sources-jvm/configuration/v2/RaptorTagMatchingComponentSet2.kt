package io.fluidsonic.raptor


internal class RaptorTagMatchingComponentSet2<out Component : RaptorTaggableComponent2>(
	private val source: RaptorComponentSet2<Component>,
	private val tagsToMatch: Set<Any>,
) : RaptorComponentSet2<Component> {

	override fun all(configure: Component.() -> Unit) {
		val configuration = RaptorTaggableComponentConfiguration2(configure = configure, tagsToMatch = tagsToMatch)

		source.all {
			tagRegistryOrCreate().addConfiguration(configuration)
		}
	}
}
