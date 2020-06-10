package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorSettings.*


internal class LookupRaptorSettings(
	private val settings: List<RaptorSettings>
) : RaptorSettings {

	override fun hasValue(path: String) =
		settings.any { it.hasValue(path) }


	override fun valueOrNull(path: String): Value? {
		for (setting in settings)
			setting.valueOrNull(path)?.let { return it }

		return null
	}
}
