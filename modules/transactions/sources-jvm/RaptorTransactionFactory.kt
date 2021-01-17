package io.fluidsonic.raptor


public interface RaptorTransactionFactory {

	public fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit = {}): RaptorTransaction


	public companion object
}
