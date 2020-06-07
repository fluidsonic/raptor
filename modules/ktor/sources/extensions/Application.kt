package io.fluidsonic.raptor

import io.ktor.application.*


@RaptorDsl
val Application.raptorContext: RaptorContext
	get() = raptorKtorServer.context
