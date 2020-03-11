package io.fluidsonic.raptor


@Raptor.Dsl3
class GraphRaptorComponent internal constructor() : RaptorComponent.Taggable {

	internal val definitions = GraphQLRaptorFeature.defaultDefinitions.toMutableList()


	internal fun complete() = GraphRaptorConfig(
		definitions = definitions
	)
}


@Raptor.Dsl3
fun RaptorConfigurable<GraphRaptorComponent>.definitions(vararg definitions: RaptorGraphDefinition) {
	definitions(definitions.asIterable())

}


@Raptor.Dsl3
fun RaptorConfigurable<GraphRaptorComponent>.definitions(definitions: Iterable<RaptorGraphDefinition>) {
	forEachComponent {
		this.definitions += definitions
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorRouteComponent>.newGraph(config: RaptorConfigurable<GraphRaptorComponent> = {}) {
	TODO()
}
