package io.fluidsonic.raptor

import io.ktor.application.*


@RaptorDsl
val ApplicationCall.raptorContext: RaptorTransactionContext
	get() = raptorKtorServerTransaction.context
