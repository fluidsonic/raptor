package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.reflect.*


internal class RootDIRaptorComponent :
	RaptorComponent.Base<RootDIRaptorComponent>(RaptorDIPlugin),
	RaptorDIComponent<RootDIRaptorComponent> {

	private val builder = DefaultRaptorDIBuilder()


	override fun provide(type: KType, provide: RaptorDI.() -> Any?) {
		builder.provide(type = type, provide = provide)
	}


	override fun toString(): String =
		"DI configuration"


	override fun RaptorComponentConfigurationEndScope<RootDIRaptorComponent>.onConfigurationEnded() {
		val module = builder.createModule("raptor")
		val factory = DefaultRaptorDI.Factory(modules = listOf(module))

		propertyRegistry.register(factory.createDI<RaptorContext>(context = lazyContext))
	}
}
