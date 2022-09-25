package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*
import kotlin.contracts.*


public interface RaptorTransaction {

	public val context: RaptorTransactionContext

	public suspend fun fail(error: Throwable)
	public suspend fun start()
	public suspend fun stop()
}


public suspend inline fun <Result> RaptorTransaction.execute(block: RaptorTransactionScope.() -> Result): Result {
	val transaction = context.transaction()
	transaction.start()

	val result = try {
		with(transaction.context) {
			block()
		}
	}
	catch (error: Throwable) {
		transaction.fail(error)
		throw error
	}

	transaction.stop()

	return result
}


public operator fun <Value : Any> RaptorTransaction.get(key: RaptorPropertyKey<out Value>): Value? =
	properties[key]


public val RaptorTransaction.properties: RaptorPropertySet
	get() = context.properties


public fun RaptorTransaction.transaction(): RaptorTransaction =
	context.transaction()


@RaptorDsl
public suspend inline fun <Result> RaptorTransaction.transaction(block: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}

	return context.transaction(block)
}
