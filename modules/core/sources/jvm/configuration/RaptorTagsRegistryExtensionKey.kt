package io.fluidsonic.raptor


private object RaptorTagsRegistryExtensionKey : RaptorComponentExtensionKey<RaptorComponentTagRegistry<*>> {

	override fun toString(): String = "tag registry"
}


@Suppress("UNCHECKED_CAST")
internal val <Component : RaptorTaggableComponent> Component.tagRegistry
	get() = extensions.getOrSet(RaptorTagsRegistryExtensionKey) { RaptorComponentTagRegistry(component = this) }
		as RaptorComponentTagRegistry<Component>
