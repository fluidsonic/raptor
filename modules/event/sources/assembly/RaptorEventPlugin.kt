package io.fluidsonic.raptor.event

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


public object RaptorEventPlugin : RaptorPluginWithConfiguration<RaptorEventPluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorEventPluginConfiguration {
		// TODO Make configurable.
		val processor = ParallelDispatchEventProcessor { _, _ -> } // FIXME Implement.
		val emitter = ProcessingEventEmitter(processor = processor)

		return RaptorEventPluginConfiguration(
			emitter = emitter,
			source = processor,
		)
	}


	override fun RaptorPluginInstallationScope.install() {
		optional(RaptorDIPlugin) {
			di.provide<RaptorEventEmitter> { context.eventEmitter }
			di.provide<RaptorEventSource> { context.eventSource }
		}

		// TODO Use lifecycle to wind up/down event handling.
	}


	override fun toString(): String = "event"
}
