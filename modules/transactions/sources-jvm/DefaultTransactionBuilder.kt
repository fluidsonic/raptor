package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal class DefaultTransactionBuilder(
	private val observers: List<RaptorTransactionObserver>,
	override val parentContext: RaptorContext,
) : RaptorTransactionConfigurationScope {

	override val lazyContext = LazyTransactionContext(parent = parentContext)
	override val propertyRegistry = RaptorPropertyRegistry.default()


	fun build(): DefaultTransaction {
		val lazyTransaction = LazyTransaction()
		propertyRegistry.register(Keys.transactionProperty, lazyTransaction)

		val context = DefaultTransactionContext(
			parent = parentContext,
			properties = propertyRegistry.toSet().withFallback(parentContext.properties),
		)
		lazyContext.resolve(context)

		val transaction = DefaultTransaction(
			context = context,
			observers = observers,
		)
		lazyTransaction.resolve(transaction)

		return transaction
	}
}
