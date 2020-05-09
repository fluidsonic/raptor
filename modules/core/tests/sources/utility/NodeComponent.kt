package tests

import io.fluidsonic.raptor.*


class NodeComponent(
	val name: String,
	parentRegistry: RaptorComponentRegistry
) : RaptorComponent.Base<NodeComponent>(), TaggableComponent {

	val _children: MutableList<NodeComponent> = mutableListOf()
	val _registry = parentRegistry.createChildRegistry()


	fun finalize(): Node = Node(
		name = name,
		children = _children.map(NodeComponent::finalize)
	)


	override fun toString() =
		"NodeComponent(name=$name, children=$_children)"
}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.node(name: String) = RaptorComponentSet.map(this) {
	val child = NodeComponent(name = name, parentRegistry = _registry)

	_children += child
	_registry.register(child)

	return@map child
}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.node(name: String, action: NodeComponent.() -> Unit) =
	node(name).forEach(action)


@RaptorDsl
val RaptorComponentSet<NodeComponent>.nodes: RaptorComponentSet<NodeComponent>
	get() = RaptorComponentSet.map(this) {
		_registry.all()
	}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.nodes(recursive: Boolean): RaptorComponentSet<NodeComponent> =
	when (recursive) {
		true -> RaptorComponentSet { action ->
			nodes {
				action()
				nodes(action)
			}
		}
		false -> nodes
	}


@RaptorDsl
fun RaptorComponentSet<NodeComponent>.nodes(recursive: Boolean, action: NodeComponent.() -> Unit) {
	nodes(recursive = recursive).forEach(action)
}
