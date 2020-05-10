package io.fluidsonic.raptor

import io.ktor.application.*


val Application.raptorContext: KtorServerContext
	get() = raptorKtorServer.context
