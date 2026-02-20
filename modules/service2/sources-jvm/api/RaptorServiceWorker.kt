package io.fluidsonic.raptor.service2

import kotlin.coroutines.*
import kotlinx.coroutines.*


/**
 * Worker interface for scheduling background work in a service's internal loop.
 *
 * Services should NOT get direct access to a CoroutineScope. Instead, they should use
 * this worker interface to schedule work that will run in the service's internal event loop.
 *
 * This is DI-injected and service-scoped (each service instance gets its own worker).
 *
 * Example usage:
 * ```kotlin
 * internal class MyService(
 *     private val worker: RaptorServiceWorker,
 * ) : RaptorService2 {
 *
 *     private fun onSomething(event: SomeEvent) {
 *         // Schedule background work via worker
 *         worker.schedule {
 *             doExpensiveWork()
 *         }
 *     }
 * }
 * ```
 */
public interface RaptorServiceWorker {

	/**
	 * Schedule a suspend block to run in the service's internal loop.
	 * The block will be executed asynchronously.
	 */
	public fun schedule(block: suspend () -> Unit): Job

	/**
	 * Schedule a suspend block to run in the service's internal loop.
	 * Returns a Deferred that can be awaited to get the result.
	 */
	public fun <T> scheduleAsync(block: suspend () -> T): Deferred<T>
}


/**
 * Default implementation of RaptorServiceWorker backed by a CoroutineScope.
 * The scope can be replaced after construction to support deferred initialization.
 */
internal class DefaultRaptorServiceWorker(
	scope: CoroutineScope,
) : RaptorServiceWorker {

	private val lock = Any()
	private var _scope: CoroutineScope = scope
	private var workScheduled = false


	/**
	 * Replace the worker's scope. This must be called before any work is scheduled.
	 */
	fun replaceScope(newScope: CoroutineScope) {
		synchronized(lock) {
			check(!workScheduled) { "Cannot replace scope after work has been scheduled." }
			_scope = newScope
		}
	}


	override fun schedule(block: suspend () -> Unit): Job {
		val scope = synchronized(lock) {
			workScheduled = true
			_scope
		}
		return scope.launch(start = CoroutineStart.UNDISPATCHED) { block() }
	}


	override fun <T> scheduleAsync(block: suspend () -> T): Deferred<T> {
		val scope = synchronized(lock) {
			workScheduled = true
			_scope
		}
		return scope.async(start = CoroutineStart.UNDISPATCHED) { block() }
	}
}


/**
 * Coroutine-safe holder for the current service worker.
 * Used during service creation to make the worker available via DI.
 *
 * Uses [ThreadContextElement] so the value survives coroutine dispatching.
 */
internal object CurrentServiceWorker {

	private val threadLocal = ThreadLocal<RaptorServiceWorker?>()


	val current: RaptorServiceWorker?
		get() = threadLocal.get()


	fun asContextElement(worker: RaptorServiceWorker): CoroutineContext.Element =
		WorkerContextElement(worker)


	private class WorkerContextElement(
		private val worker: RaptorServiceWorker,
	) : ThreadContextElement<RaptorServiceWorker?> {

		companion object Key : CoroutineContext.Key<WorkerContextElement>

		override val key: CoroutineContext.Key<WorkerContextElement> = Key


		override fun updateThreadContext(context: CoroutineContext): RaptorServiceWorker? {
			val previous = threadLocal.get()
			threadLocal.set(worker)
			return previous
		}


		override fun restoreThreadContext(context: CoroutineContext, oldState: RaptorServiceWorker?) {
			threadLocal.set(oldState)
		}
	}
}
