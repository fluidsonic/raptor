package io.fluidsonic.raptor


internal class DefaultRaptorComponentTransactionsExtension {

	val configurations: MutableList<RaptorTransactionConfigurationScope.() -> Unit> = mutableListOf()


	object Key : RaptorComponentExtensionKey<DefaultRaptorComponentTransactionsExtension> {

		override fun toString() = "transactions"
	}
}
