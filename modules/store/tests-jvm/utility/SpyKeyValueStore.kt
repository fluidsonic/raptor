package tests

import io.fluidsonic.raptor.store.*
import kotlinx.coroutines.flow.*


class SpyKeyValueStore : RaptorKeyValueStore<String, String> {

	val clearCalls = mutableListOf<Unit>()
	val getCalls = mutableListOf<String>()
	val removeCalls = mutableListOf<String>()
	val setCalls = mutableListOf<Pair<String, String>>()
	val setIfAbsentCalls = mutableListOf<Pair<String, String>>()


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
}
