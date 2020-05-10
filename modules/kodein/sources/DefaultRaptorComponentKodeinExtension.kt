package io.fluidsonic.raptor


internal class DefaultRaptorComponentKodeinExtension {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()


	object Key : RaptorComponentExtensionKey<DefaultRaptorComponentKodeinExtension> {

		override fun toString() = "kodein"
	}
}
