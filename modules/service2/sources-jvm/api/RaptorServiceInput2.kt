package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import kotlinx.coroutines.*


public interface RaptorServiceInput2<in Service : RaptorService2, out Value> { // FIXME rn

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	public fun subscribe(handler: suspend (Value) -> Unit): Job
}


/**
 * Interface for input sources that process items from a persistent queue.
 * Implementations handle the actual queue processing logic.
 */
public interface PersistentQueueInputSource2<in Service : RaptorService2, Key : Any, Value : Any> :
	RaptorServiceInput2<Service, Pair<Key, Value>> {

	/**
	 * Returns the queue from the service.
	 */
	public val queueGetter: Service.() -> Any

	/**
	 * Processes items from the queue using the provided handler.
	 * This method runs indefinitely, processing items as they arrive.
	 */
	public suspend fun processQueue(service: Service, handler: suspend (Key, Value) -> Unit)
}
