package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorSettings.*


internal class DefaultRaptorSettings(
	private val values: Map<String, Value>,
) : RaptorSettings {

	override fun hasValue(path: String) =
		values.containsKey(path)


	override fun valueOrNull(path: String): Value? =
		values[path]
}
