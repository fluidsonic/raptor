package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorSettings.*


internal class DefaultRaptorSettings(
	private val values: Map<String, Value>,
) : RaptorSettings {

	override fun hasValue(path: String) =
		splitPath(path, values::containsKey) { firstKey, remainingPath ->
			values[firstKey]?.settings()?.hasValue(remainingPath) ?: false
		}


	override fun valueOrNull(path: String): Value? =
		splitPath(path, values::get) { firstKey, remainingPath ->
			values[firstKey]?.settings()?.valueOrNull(remainingPath)
		}


	private inline fun <Result> splitPath(
		path: String,
		singleKey: (key: String) -> Result,
		multipleKeys: (firstKey: String, remainingPath: String) -> Result,
	): Result {
		val index = path.indexOf('.')
		if (index < 0)
			return singleKey(path)

		val firstKey = path.substring(0, index)
		val remainingPath = path.substring(index + 1)

		return multipleKeys(firstKey, remainingPath)
	}
}
