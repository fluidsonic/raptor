package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal class DefaultRaptorTransactionBuilder(
	override val parentContext: RaptorContext,
) : RaptorTransactionConfigurationScope {

	override val lazyContext = LazyRaptorTransactionContext(parent = parentContext)
	override val propertyRegistry = RaptorPropertyRegistry.default()


	fun build(): DefaultRaptorTransaction {
		val lazyTransaction = LazyRaptorTransaction()
		propertyRegistry.register(DefaultRaptorTransaction.PropertyKey, lazyTransaction)

		val context = DefaultRaptorTransactionContext(
			parent = parentContext,
			properties = propertyRegistry.toSet().withFallback(parentContext.properties)
		)
		lazyContext.resolve(context)

		val transaction = DefaultRaptorTransaction(context = context)
		lazyTransaction.resolve(transaction)

		return transaction
	}
}
