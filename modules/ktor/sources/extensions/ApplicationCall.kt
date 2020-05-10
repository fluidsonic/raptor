package io.fluidsonic.raptor

import io.ktor.application.*


val ApplicationCall.raptorContext: KtorServerContext
	get() = raptorKtorServerTransaction.context
