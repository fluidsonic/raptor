package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*
import kotlin.contracts.*


public fun Raptor.transaction(): RaptorTransaction =
	context.transaction()


@RaptorDsl
public suspend inline fun <Result> Raptor.transaction(block: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}

	return context.transaction(block)
}
