package io.fluidsonic.raptor


internal class RaptorTaggableComponentConfiguration2<in Component : RaptorTaggableComponent2>(
	val configure: Component.() -> Unit,
	val tagsToMatch: Set<Any>,
) {

	init {
		require(tagsToMatch.isNotEmpty()) { "'tagsToMatch' must not be empty." }
	}
}
