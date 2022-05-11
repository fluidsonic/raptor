package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorSettings.*


internal class MapRaptorSettings(
	private val valueProviders: Map<String, ValueProvider<*>>,
) : RaptorSettings {

	override fun valueProvider(path: String): ValueProvider<*>? =
		split(path) { key, remainingPath ->
			when (remainingPath) {
				null -> valueProviders[key]
				else -> (valueProviders[key]?.value as? RaptorSettings?)?.valueProvider(remainingPath) // TODO Improve - what if not RaptorSettings?
			}
		}


	private inline fun <Result> split(
		path: String,
		action: (key: String, remainingPath: String?) -> Result,
	): Result {
		val index = path.indexOf('.')
		if (index < 0)
			return action(path, null)

		val key = path.substring(0, index)
		val remainingPath = path.substring(index + 1)

		return action(key, remainingPath)
	}
}
