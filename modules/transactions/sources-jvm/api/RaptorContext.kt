package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public fun RaptorContext.transaction(): RaptorTransaction =
	transactionFactory.createTransaction(context = this)


internal val RaptorContext.transactionFactory: RaptorTransactionFactory
	get() = properties[Keys.transactionFactoryProperty] ?: throw RaptorPluginNotInstalledException(RaptorTransactionPlugin)
