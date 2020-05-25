package io.fluidsonic.raptor


interface RaptorTransaction {

	val context: RaptorTransactionContext


	companion object
}


fun RaptorTransaction.createTransaction(): RaptorTransaction =
	context.createTransaction()


operator fun <Value : Any> RaptorTransaction.get(key: RaptorPropertyKey<out Value>) =
	properties[key]


val RaptorTransaction.properties: RaptorPropertySet
	get() = context.properties
