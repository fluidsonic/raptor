package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


internal class RootDIRaptorComponent :
	RaptorComponent.Base<RootDIRaptorComponent>(RaptorDIPlugin),
	RaptorDIComponent<RootDIRaptorComponent> {

	private val builder = DefaultRaptorDIBuilder()


	override fun <Value : Any> provide(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value) {
		builder.provide(key = key, provide = provide)
	}


	override fun <Value : Any> provideOptional(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value?) {
		builder.provide(key = key, provide = provide)
	}


	override fun toString(): String =
		"DI configuration"


	override fun RaptorComponentConfigurationEndScope<RootDIRaptorComponent>.onConfigurationEnded() {
		val module = builder.createModule("raptor")
		val factory = DefaultRaptorDI.Factory(modules = listOf(module))

		propertyRegistry.register(factory.createDI<RaptorContext>(context = lazyContext))
	}
}
