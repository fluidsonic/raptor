package tests

import io.fluidsonic.raptor.*


class NodeComponent(
	val name: String,
	val registry: RaptorComponentRegistry
) : RaptorComponent<NodeComponent> {

	val children: MutableList<NodeComponent> = mutableListOf()


	fun finalize(): Node = Node(
		name = name,
		children = children.map(NodeComponent::finalize)
	)


	override fun toString() =
		"NodeComponent(name=$name, children=$children)"
}


fun RaptorComponentSet<NodeComponent>.node(name: String) = RaptorComponentSet.map(this) {
	val child = NodeComponent(name = name, registry = registry)

	children += child
	registry.register(child)

	return@map child
}
