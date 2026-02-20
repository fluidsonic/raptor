package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.reflect.*


internal class RaptorServiceRegistration2<Service : RaptorService2>(
	val errorHandler: RaptorServiceComponent2.ErrorHandler,
	val factory: RaptorDI.() -> Service,
	val inputSources: List<RaptorServiceInputRegistration<Service, *>>,
	val name: String,
) : RaptorComponent.Base<RaptorServiceComponent2<Service>>(RaptorLifecyclePlugin) {

	val diKey = ServiceDIKey2<Service>(name)


	fun install(di: RaptorDIComponent<*>) {
		di.provide(diKey, factory)
	}


	companion object {

		/**
		 * Registers a DI provider for RaptorServiceWorker that looks up the current worker
		 * from the thread-local holder. This is called once during plugin completion.
		 */
		fun installWorkerProvider(di: RaptorDIComponent<*>) {
			di.provide<RaptorServiceWorker>(typeOf<RaptorServiceWorker>()) {
				CurrentServiceWorker.current
					?: error("RaptorServiceWorker is only available during service construction.")
			}
		}
	}
}
