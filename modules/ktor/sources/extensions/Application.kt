package io.fluidsonic.raptor

import io.ktor.application.*


val Application.raptorContext: RaptorContext
	get() = raptorKtorServer.context
