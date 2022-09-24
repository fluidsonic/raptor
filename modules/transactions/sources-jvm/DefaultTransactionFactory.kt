package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal class DefaultTransactionFactory(
	private val configurations: List<RaptorTransactionConfigurationScope.() -> Unit>,
	private val observers: List<RaptorTransactionObserver>,
) : RaptorTransactionFactory {

	override fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit): DefaultTransaction =
		DefaultTransactionBuilder(observers = observers, parentContext = context)
			.apply {
				for (staticConfiguration in configurations)
					staticConfiguration()

				configuration()
			}
			.build()
}
