package io.fluidsonic.raptor


private object RaptorTagsRegistryExtensionKey2 : RaptorComponentExtensionKey<RaptorComponentTagRegistry2<*>> {

	override fun toString(): String = "tag registry"
}


@Suppress("UNCHECKED_CAST")
internal fun <Component : RaptorTaggableComponent2> Component.tagRegistry() =
	extensions[RaptorTagsRegistryExtensionKey2] as RaptorComponentTagRegistry2<Component>?


internal fun <Component : RaptorTaggableComponent2> Component.tagRegistryOrCreate() =
	tagRegistry() ?: RaptorComponentTagRegistry2(component = this).also { extensions[RaptorTagsRegistryExtensionKey2] = it }
