package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal object Keys {

	val transactionProperty = RaptorPropertyKey<RaptorTransaction>("transaction")
	val transactionFactoryProperty = RaptorPropertyKey<RaptorTransactionFactory>("transaction factory")
	val transactionsComponent = RaptorComponentKey<RaptorTransactionsComponent>("transactions")
}
