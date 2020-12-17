package io.fluidsonic.raptor


internal class RaptorComponentTransactionConfigurationExtension {

	val configurations: MutableList<RaptorTransactionConfigurationScope.() -> Unit> = mutableListOf()


	object Key : RaptorComponentExtensionKey<RaptorComponentTransactionConfigurationExtension> {

		override fun toString() = "transactions"
	}
}
