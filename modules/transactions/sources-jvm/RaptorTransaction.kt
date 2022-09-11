package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public interface RaptorTransaction {

	public val context: RaptorTransactionContext


	public companion object
}


public fun RaptorTransaction.createTransaction(): RaptorTransaction =
	context.createTransaction()


public operator fun <Value : Any> RaptorTransaction.get(key: RaptorPropertyKey<out Value>): Value? =
	properties[key]


public val RaptorTransaction.properties: RaptorPropertySet
	get() = context.properties
