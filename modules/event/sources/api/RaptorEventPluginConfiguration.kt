package io.fluidsonic.raptor.event


public data class RaptorEventPluginConfiguration(
	val emitter: RaptorEventEmitter,
	val source: RaptorEventSource,
)
