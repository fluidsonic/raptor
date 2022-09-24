package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal class DefaultTransactionContext(
	override val parent: RaptorContext,
	override val properties: RaptorPropertySet,
) : RaptorTransactionContext {

	override fun toString() =
		"transaction context (FIXME)"
}
