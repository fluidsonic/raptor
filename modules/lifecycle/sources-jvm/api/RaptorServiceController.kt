package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.di.*
import kotlinx.coroutines.*
import org.slf4j.*


internal class RaptorServiceController(
	private val diKey: ServiceDIKey,
	private val name: String,
) {

	private var service: RaptorService? = null


	suspend fun createIn(scope: CoroutineScope, di: RaptorDI, logger: Logger) {
		check(service == null)

		val service = di.get(diKey)
		service.createIn(scope, logger = logger, name = name)

		this.service = service
	}


	fun start() {
		checkNotNull(service).start()
	}


	suspend fun stop() {
		try {
			checkNotNull(service).stop()
		}
		finally {
			service = null
		}
	}


	override fun toString() =
		"RaptorServiceController '$name'"
}
