package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public interface RaptorTransactionFactory {

	public fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit = {}): RaptorTransaction


	public companion object {

		public val empty: RaptorTransactionFactory = DefaultRaptorTransactionFactory(configurations = emptyList())
	}
}


internal object RaptorTransactionFactoryPropertyKey : RaptorPropertyKey<RaptorTransactionFactory> {

	override fun toString() = "transaction factory"
}
