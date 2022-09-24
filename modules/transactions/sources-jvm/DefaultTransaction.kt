package io.fluidsonic.raptor.transactions


internal class DefaultTransaction(
	override val context: DefaultTransactionContext,
	private val observers: List<RaptorTransactionObserver>,
) : RaptorTransaction {

	private var state = State.created


	override suspend fun fail(error: Throwable) {
		when (state) {
			State.created -> error("Cannot fail a transaction that has not yet started.")
			State.failed -> error("Cannot fail a transaction that has already failed.")
			State.started -> {
				val observerCount = observers.size
				var index = observerCount - 1

				while (index >= 0) {
					val observer = observers[index]
					try {
						observer.onFail(context, error)
					}
					catch (e: Throwable) {
						error.addSuppressed(e)
					}

					index -= 1
				}

				state = State.failed
			}

			State.stopped -> error("Cannot fail a transaction that has already stopped.")
		}
	}


	override suspend fun start() {
		when (state) {
			State.created -> {
				var index = 0
				val observerCount = observers.size
				var error: Throwable? = null

				while (index < observerCount) {
					val observer = observers[index]
					try {
						observer.onStart(context)
						index += 1
					}
					catch (e: Throwable) {
						error = e
						break
					}
				}

				when (error) {
					null -> state = State.started
					else -> {
						while (index > 0) {
							index -= 1

							val observer = observers[index]
							try {
								observer.onFail(context, error)
							}
							catch (e: Throwable) {
								error.addSuppressed(e)
							}
						}

						state = State.failed
						throw error
					}
				}
			}

			State.failed -> error("Cannot start a transaction that has failed.")
			State.started -> error("Cannot start a transaction that has already started.")
			State.stopped -> error("Cannot start a transaction that has already stopped.")
		}
	}


	override suspend fun stop() {
		when (state) {
			State.created -> error("Cannot stop a transaction that has not yet started.")
			State.failed -> error("Cannot stop a transaction that has failed.")
			State.started -> {
				val observerCount = observers.size
				var index = observerCount - 1
				var error: Throwable? = null

				while (index >= 0) {
					val observer = observers[index]
					try {
						observer.onStop(context)
						index -= 1
					}
					catch (e: Throwable) {
						error = e
						break
					}
				}

				when (error) {
					null -> state = State.stopped
					else -> {
						while (index > 0) {
							index -= 1

							val observer = observers[index]
							try {
								observer.onFail(context, error)
							}
							catch (e: Throwable) {
								error.addSuppressed(e)
							}
						}

						state = State.failed
						throw error
					}
				}
			}

			State.stopped -> error("Cannot stop a transaction that has already stopped.")
		}
	}


	private enum class State {

		created,
		failed,
		started,
		stopped,
	}
}
