package tests

import io.fluidsonic.raptor.*


class NodeComponent(
	@RaptorDsl val name: String
) : RaptorComponent.Base<NodeComponent>(), RaptorComponentContainer, TaggableComponent {

	fun finalize(): Node = Node(
		name = name,
		children = childComponentRegistry.many(Key).map(NodeComponent::finalize)
	)


	override fun toString() =
		"node ($name)"


	object Key : RaptorComponentKey<NodeComponent> {

		override fun toString() = "node"
	}
}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.node(name: String) = RaptorComponentSet.map(this) {
	val child = NodeComponent(name = name)

	childComponentRegistry.register(NodeComponent.Key, child)

	return@map child
}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.node(name: String, action: NodeComponent.() -> Unit) =
	node(name).configure(action)


@RaptorDsl
val RaptorComponentSet<NodeComponent>.nodes: RaptorComponentSet<NodeComponent>
	get() = RaptorComponentSet.map(this) {
		childComponentRegistry.configure(NodeComponent.Key)
	}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.nodes(recursive: Boolean): RaptorComponentSet<NodeComponent> =
	when (recursive) {
		true -> RaptorComponentSet { action ->
			nodes {
				action()
				nodes(recursive = true).configure(action)
			}
		}
		false -> nodes
	}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.nodes(recursive: Boolean, action: NodeComponent.() -> Unit) {
	nodes(recursive = recursive).configure(action)
}
