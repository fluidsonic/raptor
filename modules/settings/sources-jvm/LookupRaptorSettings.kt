package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorSettings.*


internal class LookupRaptorSettings(
	private val settings: List<RaptorSettings>,
) : RaptorSettings {

	override fun valueProvider(path: String): ValueProvider<*>? {
		for (setting in settings)
			setting.valueProvider(path)?.let { return it }

		return null
	}
}
