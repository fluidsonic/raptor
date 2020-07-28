package io.fluidsonic.raptor


interface RaptorTransactionFactory {

	fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit = {}): RaptorTransaction


	companion object
}
