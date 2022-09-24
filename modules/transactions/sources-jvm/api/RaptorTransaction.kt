package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*
import kotlin.contracts.*


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
