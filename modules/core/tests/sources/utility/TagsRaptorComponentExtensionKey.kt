package tests

import io.fluidsonic.raptor.*


object TagsRaptorComponentExtensionKey : RaptorComponentExtensionKey<MutableSet<Any>> {

	override fun toString() = "tags"
}
