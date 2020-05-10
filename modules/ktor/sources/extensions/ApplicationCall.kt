package io.fluidsonic.raptor

import io.ktor.application.*


val ApplicationCall.raptorContext: RaptorContext
	get() = raptorKtorServerTransaction.context
