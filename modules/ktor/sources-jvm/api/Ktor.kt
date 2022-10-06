package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*


@RaptorDsl
public val Application.raptorContext: RaptorContext
	get() = raptorServerInternal.context


@RaptorDsl
public val Application.raptorServer: RaptorKtorServer
	get() = raptorServerInternal


internal val Application.raptorServerInternal: RaptorKtorServerInternal
	get() = attributes.getOrNull(Keys.serverKtorAttribute) ?: throw RaptorPluginNotInstalledException(RaptorKtorPlugin)


@RaptorDsl
public val ApplicationCall.raptorContext: RaptorTransactionContext
	get() = raptorTransaction.context


@RaptorDsl
public val PipelineContext<*, out ApplicationCall>.raptorContext: RaptorTransactionContext
	get() = context.raptorContext


@RaptorDsl
public val Route.raptorContext: RaptorContext
	get() = application.raptorContext


@RaptorDsl
public val WebSocketServerSession.raptorContext: RaptorTransactionContext
	get() = call.raptorContext
