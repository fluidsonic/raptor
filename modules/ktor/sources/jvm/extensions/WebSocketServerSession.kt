package io.fluidsonic.raptor

import io.ktor.websocket.*


@RaptorDsl
public val WebSocketServerSession.raptorContext: RaptorTransactionContext
	get() = call.raptorContext
