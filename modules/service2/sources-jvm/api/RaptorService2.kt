package io.fluidsonic.raptor.service2

import kotlin.coroutines.*


// FIXME do we need a way to collect relevant event data at time of event before the task gets added to the channel?
// FIXME check all for concurrency
// FIXME some services can build long backlog and fall behind - how to handle? add monitoring
public interface RaptorService2 {

	public data class Error(
		val coroutineContext: CoroutineContext,
		val throwable: Throwable,
	)
}
