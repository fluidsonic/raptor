package tests

import io.fluidsonic.raptor.*


class NodeComponent(
	@RaptorDsl val name: String
) : RaptorComponent.Base<NodeComponent>(), TaggableComponent {

	fun finalize(): Node = Node(
		name = name,
		children = componentRegistry.many(Key).map(NodeComponent::finalize)
	)


	override fun toString() =
		"node ($name)"


	object Key : RaptorComponentKey<NodeComponent> {

		override fun toString() = "node"
	}
}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.node(name: String) = withComponentAuthoring {
	map {
		NodeComponent(name = name)
			.also { componentRegistry.register(NodeComponent.Key, it) }
	}
}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.node(name: String, action: NodeComponent.() -> Unit) =
	node(name).configure(action)


@RaptorDsl
val RaptorComponentSet<NodeComponent>.nodes: RaptorComponentSet<NodeComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(NodeComponent.Key)
		}
	}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.nodes(recursive: Boolean): RaptorComponentSet<NodeComponent> =
	withComponentAuthoring {
		when (recursive) {
			true -> componentSet { action ->
				authoredSet.nodes {
					action()
					nodes(recursive = true).configure(action)
				}
			}
			false -> authoredSet.nodes
		}
	}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.nodes(recursive: Boolean, action: NodeComponent.() -> Unit) {
	nodes(recursive = recursive).configure(action)
}
