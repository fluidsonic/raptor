package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public object RaptorTransactionPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		propertyRegistry.register(Keys.transactionFactoryProperty, componentRegistry.one(Keys.transactionsComponent).toFactory())
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.transactionsComponent, RaptorTransactionsComponent())
	}


	override fun toString(): String = "transaction"
}
