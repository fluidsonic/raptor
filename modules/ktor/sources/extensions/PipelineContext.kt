package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.pipeline.*


val PipelineContext<Unit, ApplicationCall>.raptorContext: KtorServerTransactionContext
	get() = raptorKtorServerTransaction.context


internal val PipelineContext<Unit, ApplicationCall>.raptorKtorServerTransaction
	get() = context.raptorKtorServerTransaction
