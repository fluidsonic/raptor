package io.fluidsonic.raptor.graph


@Suppress("MemberVisibilityCanBePrivate") // TODO Add tests.
public data class RaptorGraphPluginConfiguration(
	val graphs: Collection<RaptorGraph>,
) {

	public fun singleGraph(): RaptorGraph =
		when (graphs.size) {
			0 -> error("There's no graph but one was expected.")
			1 -> graphs.single()
			else -> error("There are multiple graphs but exactly one was expected.")
		}


	public fun taggedGraph(tag: Any?): RaptorGraph =
		when (tag) {
			null -> singleGraph()
			else -> when (graphs.isEmpty()) {
				true -> error("There's no graph but one was expected with tag: $tag")
				false -> graphs.singleOrNull { it.tags.contains(tag) } ?: error("There are multiple graphs but exactly one was expected with tag: $tag")
			}
		}
}
