package io.fluidsonic.raptor


internal class RaptorTagMatchingComponentQuery<Component : RaptorTaggableComponent<Component>>(
	private val source: RaptorAssemblyQuery<Component>,
	private val tagsToMatch: Set<Any>,
) : RaptorAssemblyQuery<Component> {

	override fun each(configure: Component.() -> Unit) {
		val configuration = RaptorTaggableComponentConfiguration(configure = configure, tagsToMatch = tagsToMatch)

		source {
			tagRegistryOrCreate().addConfiguration(configuration)
		}
	}
}
