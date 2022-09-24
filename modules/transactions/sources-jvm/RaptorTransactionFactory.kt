package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public interface RaptorTransactionFactory {

	public fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit = {}): RaptorTransaction


	public companion object {

		public val empty: RaptorTransactionFactory = DefaultTransactionFactory(
			configurations = emptyList(),
			observers = emptyList(),
		)
	}
}
