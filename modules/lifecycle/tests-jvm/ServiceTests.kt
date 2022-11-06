package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.raptor.lifecycle.RaptorLifecycle.*
import kotlin.coroutines.*
import kotlin.test.*
import kotlin.test.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.slf4j.*


@OptIn(ExperimentalCoroutinesApi::class)
class ServiceTests {

	@Test
	fun testBasics() = runBlocking {
		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service(::PrintService)
		}

		raptor.lifecycle.startIn(this)
		raptor.lifecycle.stop()
	}


	private class PrintService(
		private val logger: Logger,
	) : RaptorService() {

		override suspend fun RaptorServiceCreationScope.created() {
			logger.info("Service created.")
		}


		override fun RaptorServiceExceptionScope.exceptionRaised(coroutineContext: CoroutineContext, exception: Throwable) {
			logger.info("Exception raised.", exception)
		}


		override fun RaptorServiceStartScope.started() {
			logger.info("Service started.")
		}


		override suspend fun RaptorServiceStopScope.stopped() {
			logger.info("Service stopped.")
		}
	}
}
