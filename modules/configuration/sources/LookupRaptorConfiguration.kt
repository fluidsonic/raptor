package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorConfiguration.*


internal class LookupRaptorConfiguration(
	private val configurations: List<RaptorConfiguration>
) : RaptorConfiguration {

	override fun hasValue(path: String) =
		configurations.any { it.hasValue(path) }


	override fun valueOrNull(path: String): Value? {
		for (configuration in configurations)
			configuration.valueOrNull(path)?.let { return it }

		return null
	}
}
