package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.pipeline.*


@RaptorDsl
val PipelineContext<Unit, ApplicationCall>.raptorContext: RaptorTransactionContext
	get() = raptorKtorServerTransaction.context


internal val PipelineContext<Unit, ApplicationCall>.raptorKtorServerTransaction
	get() = context.raptorKtorServerTransaction
