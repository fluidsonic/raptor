package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public object RaptorTransactionFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(Keys.transactionFactoryProperty, componentRegistry.one(Keys.transactionsComponent).toFactory())
	}


	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(Keys.transactionsComponent, RaptorTransactionsComponent())
	}


	override fun toString(): String = "transaction"
}
