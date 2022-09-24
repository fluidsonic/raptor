package tests

import io.fluidsonic.raptor.*


class NodeComponent(
	@RaptorDsl val name: String,
) : RaptorComponent.Base<NodeComponent>(), TaggableComponent<NodeComponent> {

	@RaptorDsl
	fun node(name: String) =
		NodeComponent(name = name).also { componentRegistry.register(key, it) }


	@RaptorDsl
	val nodes: RaptorComponentSet<NodeComponent>
		get() = componentRegistry.all(key)


	@RaptorDsl
	fun nodes(recursive: Boolean): RaptorAssemblyQuery<NodeComponent> =
		when (recursive) {
			true -> RaptorAssemblyQuery { action ->
				nodes.all {
					action()
					nodes(recursive = true).each(action)
				}
			}

			false -> nodes.all
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
val RaptorAssemblyQuery<NodeComponent>.nodes: RaptorAssemblyQuery<NodeComponent>
	get() = flatMap { it.nodes.all }


@RaptorDsl
fun RaptorAssemblyQuery<NodeComponent>.nodes(recursive: Boolean): RaptorAssemblyQuery<NodeComponent> =
	flatMap { it.nodes(recursive = recursive) }


@RaptorDsl
fun RaptorAssemblyQuery<NodeComponent>.nodes(recursive: Boolean, action: NodeComponent.() -> Unit) {
	nodes(recursive = recursive)(action)
}
