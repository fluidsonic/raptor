package tests

import io.fluidsonic.raptor.*


class NodeComponent(
	@RaptorDsl val name: String,
) : RaptorComponent.Base<NodeComponent>(NodePlugin),
	RaptorComponentSet<NodeComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery<NodeComponent>
		get() = componentRegistry.all(key).all


	@RaptorDsl
	fun node(name: String) =
		NodeComponent(name = name).also { componentRegistry.register(key, it) }


	@RaptorDsl
	fun nodes(recursive: Boolean): RaptorAssemblyQuery<NodeComponent> =
		when (recursive) {
			true -> RaptorAssemblyQuery { action ->
				all {
					action()
					nodes(recursive = true).each(action)
				}
			}

			false -> all
		}


	fun toNode(): Node = Node(
		name = name,
		children = componentRegistry.many(key).map(NodeComponent::toNode)
	)


	override fun toString() =
		"node ($name)"


	companion object {

		val key = RaptorComponentKey<NodeComponent>("node")
	}
}


@RaptorDsl
fun RaptorAssemblyQuery<NodeComponent>.node(name: String) =
	map { node ->
		node.node(name)
	}


@RaptorDsl
fun RaptorAssemblyQuery<NodeComponent>.node(name: String, action: NodeComponent.() -> Unit) =
	node(name).each(action)


@RaptorDsl
fun RaptorAssemblyQuery<NodeComponent>.all(recursive: Boolean): RaptorAssemblyQuery<NodeComponent> =
	flatMap { it.nodes(recursive = recursive) }


@RaptorDsl
fun RaptorAssemblyQuery<NodeComponent>.all(recursive: Boolean, action: NodeComponent.() -> Unit) {
	all(recursive = recursive)(action)
}
