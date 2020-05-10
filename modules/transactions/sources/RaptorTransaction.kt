package io.fluidsonic.raptor


interface RaptorTransaction {

	val context: RaptorTransactionContext


	companion object
}


fun RaptorTransaction.createTransaction(): RaptorTransaction =
	context.createTransaction()


fun RaptorTransaction.asScope(): RaptorTransactionScope =
	context.asScope()


operator fun <Value : Any> RaptorTransaction.get(key: RaptorTransactionPropertyKey<out Value>) =
	properties[key]


val RaptorTransaction.properties: RaptorTransactionPropertySet
	get() = context.properties


fun Raptor.createTransaction(): RaptorTransaction =
	context.createTransaction()


inline fun <Result> Raptor.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	context.withNewTransaction(block)


fun RaptorContext.createTransaction(): RaptorTransaction =
	properties[DefaultRaptorTransactionFactory.PropertyKey]?.createTransaction(context = this)
		?: error("You must install ${RaptorTransactionFeature::class.simpleName} in order to use transactions.")


inline fun <Result> RaptorContext.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	with(createTransaction().asScope()) {
		block()
	}


inline fun <Result> RaptorScope.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	context.withNewTransaction(block)
