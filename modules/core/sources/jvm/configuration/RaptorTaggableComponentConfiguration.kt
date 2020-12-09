package io.fluidsonic.raptor


internal class RaptorTaggableComponentConfiguration<in Component : RaptorTaggableComponent>(
	val action: Component.() -> Unit,
	val tagsToMatch: Set<Any>,
) {

	init {
		require(tagsToMatch.isNotEmpty()) { "'tagsToMatch' must not be empty." }
	}
}
