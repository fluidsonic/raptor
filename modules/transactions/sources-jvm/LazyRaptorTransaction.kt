package io.fluidsonic.raptor.transactions


public class LazyRaptorTransaction : RaptorTransaction {

	private var delegate: RaptorTransaction? = null


	override val context: RaptorTransactionContext
		get() = requireDelegate().context


	private fun requireDelegate() =
		delegate ?: error("This transaction cannot be used until its configuration has completed.")


	public fun resolve(transaction: RaptorTransaction) {
		check(delegate == null)

		delegate = transaction
	}


	override fun toString(): String =
		delegate?.toString() ?: "<lazy transaction waiting for configuration to complete>"
}
