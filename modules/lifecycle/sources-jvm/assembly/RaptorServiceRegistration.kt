package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


internal class RaptorServiceRegistration<Service : RaptorService>(
	val factory: RaptorDI.() -> Service,
	val name: String,
	private val providedKeys: List<RaptorDIKey<out Service>>,
) : RaptorComponent.Base<RaptorServiceComponent<Service>>(RaptorLifecyclePlugin) {

	val diKey = ServiceDIKey<Service>(name)


	fun install(di: RaptorDIComponent<*>) {
		val diKey = diKey
		di.provide(diKey, factory)

		// TODO Improve.
		@Suppress("UNCHECKED_CAST")
		for (key in providedKeys)
			di.provide(key as RaptorDIKey<Service>) { get(diKey) }
	}
}
