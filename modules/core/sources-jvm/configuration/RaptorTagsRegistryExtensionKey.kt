package io.fluidsonic.raptor


private object RaptorTagsRegistryExtensionKey : RaptorComponentExtensionKey<RaptorComponentTagRegistry<*>> {

	override fun toString(): String = "tag registry"
}


@Suppress("UNCHECKED_CAST")
internal fun <Component : RaptorTaggableComponent> Component.tagRegistry() =
	extensions[RaptorTagsRegistryExtensionKey] as RaptorComponentTagRegistry<Component>?


@Suppress("UNCHECKED_CAST")
internal fun <Component : RaptorTaggableComponent> Component.tagRegistryOrCreate() =
	tagRegistry() ?: RaptorComponentTagRegistry(component = this).also { extensions[RaptorTagsRegistryExtensionKey] = it }
