package io.fluidsonic.raptor.event

import io.fluidsonic.raptor.*


@RaptorDsl
public val RaptorScope.eventEmitter: RaptorEventEmitter
	get() = context.plugins.event.emitter


@RaptorDsl
public val RaptorScope.eventSource: RaptorEventSource
	get() = context.plugins.event.source
