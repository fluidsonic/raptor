package io.fluidsonic.raptor


internal class RaptorTagMatchingAssemblyQuery2<out Component : RaptorTaggableComponent2>(
	private val source: RaptorAssemblyQuery2<Component>,
	private val tagsToMatch: Set<Any>,
) : RaptorAssemblyQuery2<Component> {

	override fun invoke(configure: Component.() -> Unit) {
		val configuration = RaptorTaggableComponentConfiguration2(configure = configure, tagsToMatch = tagsToMatch)

		source {
			tagRegistryOrCreate().addConfiguration(configuration)
		}
	}
}
