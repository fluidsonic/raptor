package io.fluidsonic.raptor.transactions


internal class LazyTransaction : RaptorTransaction {

	private var delegate: RaptorTransaction? = null


	override val context: RaptorTransactionContext
		get() = requireDelegate().context


	override suspend fun fail(error: Throwable) {
		requireDelegate().fail(error)
	}


	private fun requireDelegate() =
		delegate ?: error("This transaction cannot be used until its configuration has completed.")


	fun resolve(transaction: RaptorTransaction) {
		check(delegate == null)

		delegate = transaction
	}


	override suspend fun start() {
		requireDelegate().start()
	}


	override suspend fun stop() {
		requireDelegate().stop()
	}


	override fun toString(): String =
		delegate?.toString() ?: "<lazy transaction waiting for configuration to complete>"
}
