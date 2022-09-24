package io.fluidsonic.raptor


internal class RaptorTaggableComponentConfiguration<in Component : RaptorTaggableComponent<in Component>>(
	val configure: Component.() -> Unit,
	val tagsToMatch: Set<Any>,
) {

	init {
		require(tagsToMatch.isNotEmpty()) { "'tagsToMatch' must not be empty." }
	}
}
