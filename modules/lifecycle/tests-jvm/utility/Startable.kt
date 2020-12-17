package tests

import kotlinx.coroutines.*


class Startable(
	private val delayInMilliseconds: Long
) {

	var isStarted = false
		private set


	suspend fun start() {
		check(!isStarted) { "Already started." }

		isStarted = true

		delay(delayInMilliseconds)
	}


	suspend fun stop() {
		check(isStarted) { "Not started." }

		isStarted = false

		delay(delayInMilliseconds)
	}
}
