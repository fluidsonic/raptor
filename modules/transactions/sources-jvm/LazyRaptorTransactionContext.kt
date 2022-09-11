package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal class LazyRaptorTransactionContext(
	override val parent: RaptorContext,
) : RaptorTransactionContext.Lazy {

	private var delegate: RaptorTransactionContext? = null


	override val context: RaptorTransactionContext
		get() = requireDelegate()


	override val properties: RaptorPropertySet
		get() = requireDelegate().properties


	private fun requireDelegate() =
		delegate ?: error("This context cannot be used until the configuration of the transaction has completed.")


	fun resolve(context: RaptorTransactionContext) {
		check(delegate == null)

		delegate = context
	}


	override fun toString() =
		delegate?.toString() ?: "<lazy transaction context waiting for configuration to complete>"
}
