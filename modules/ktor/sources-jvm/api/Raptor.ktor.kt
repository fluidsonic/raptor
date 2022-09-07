package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*


@RaptorDsl
public val Application.raptorContext: RaptorContext
	get() = raptorServerImpl.context


@RaptorDsl
public val Application.raptorServer: RaptorKtorServer
	get() = raptorServerImpl


@RaptorDsl
public val ApplicationCall.raptorContext: RaptorTransactionContext
	get() = raptorTransaction.context


@RaptorDsl
public val PipelineContext<*, out ApplicationCall>.raptorContext: RaptorTransactionContext
	get() = context.raptorContext


public val RaptorContext.ktor: RaptorKtor
	get() = properties[RaptorKtorImpl.PropertyKey]
		?: error("You must install ${RaptorKtorFeature::class.simpleName} for enabling Ktor functionality.")


@RaptorDsl
public val Route.raptorContext: RaptorContext
	get() = application.raptorContext


@RaptorDsl
public val WebSocketServerSession.raptorContext: RaptorTransactionContext
	get() = call.raptorContext
