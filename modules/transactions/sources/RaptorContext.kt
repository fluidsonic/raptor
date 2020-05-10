package io.fluidsonic.raptor


interface RaptorContext : RaptorScope {

	fun createTransaction(): RaptorTransaction


	companion object
}


val Raptor.context: RaptorContext
	get() = this[DefaultRaptorContext.PropertyKey]
		?: error("You must install the ${RaptorTransactionContext::class.simpleName} in order to access the context.")
