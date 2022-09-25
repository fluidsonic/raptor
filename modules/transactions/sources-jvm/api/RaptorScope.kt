package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*
import kotlin.contracts.*


@RaptorDsl
public suspend inline fun <Result> RaptorScope.transaction(block: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}

	return context.transaction().execute(block)
}
