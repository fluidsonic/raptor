package io.fluidsonic.raptor.mongo2

import kotlinx.coroutines.reactive.*
import org.reactivestreams.*


internal suspend fun Publisher<out Void>.awaitEmpty() {
	awaitFirstOrNull()?.let {
		error("Expected an empty result but received at least one element: $it")
	}
}
