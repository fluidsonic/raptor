package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*
import kotlin.contracts.*


@RaptorDsl
public suspend inline fun <Result> RaptorScope.transaction(block: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}

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
