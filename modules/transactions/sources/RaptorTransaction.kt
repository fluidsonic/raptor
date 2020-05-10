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
