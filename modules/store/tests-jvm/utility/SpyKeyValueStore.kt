package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.RaptorKeyValueStore.*
import kotlinx.coroutines.flow.*


class SpyKeyValueStore : RaptorKeyValueStore<String, String> {

	private val values = mutableMapOf<String, String>()

	val clearCalls = mutableListOf<Unit>()
	val getCalls = mutableListOf<String>()
	val removeCalls = mutableListOf<String>()
	val setCalls = mutableListOf<Pair<String, String>>()
	val setIfAbsentCalls = mutableListOf<Pair<String, String>>()
	val updateCalls = mutableListOf<String>()


	override suspend fun clear() {
		clearCalls += Unit
	}

	override fun entries(): Flow<Pair<String, String>> = emptyFlow()

	override fun keys(): Flow<String> = emptyFlow()

	override fun values(): Flow<String> = emptyFlow()

	override suspend fun get(key: String): String? {
		getCalls += key
		return null
	}

	override suspend fun remove(key: String): Boolean {
		removeCalls += key
		return false
	}

	override suspend fun set(key: String, value: String) {
		setCalls += key to value
	}

	override suspend fun setIfAbsent(key: String, value: String): Boolean {
		setIfAbsentCalls += key to value
		return true
	}

	override suspend fun update(
		key: String,
		maxAttempts: Int,
		decide: (current: String?) -> UpdateDecision<String>,
	): String? {
		updateCalls += key

		return when (val decision = decide(values[key])) {
			is UpdateDecision.Keep -> values[key]
			is UpdateDecision.Remove -> {
				values.remove(key)

				null
			}
			is UpdateDecision.Update -> {
				values[key] = decision.value

				decision.value
			}
		}
	}
}
