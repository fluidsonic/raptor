package io.fluidsonic.raptor


class GraphQLRaptorSetup internal constructor() {

	private val definitions = GraphQLRaptorFeature.defaultDefinitions.toMutableList()


	internal fun complete() = GraphQLRaptorConfig(
		definitions = definitions
	)


	fun definitions(vararg definitions: RaptorGraphDefinition) =
		definitions(definitions.asIterable())


	fun definitions(definitions: Iterable<RaptorGraphDefinition>) {
		this.definitions += definitions
	}
}
