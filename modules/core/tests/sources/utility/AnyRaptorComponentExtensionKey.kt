package tests

import io.fluidsonic.raptor.*


object AnyRaptorComponentExtensionKey : RaptorComponentExtensionKey<Any> {

	override fun toString() = "any"
}
