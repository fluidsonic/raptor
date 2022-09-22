package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.reflect.*


internal class RootDIRaptorComponent : RaptorComponent2.Base<RootDIRaptorComponent>(), RaptorDIComponent {

	private val builder = DefaultRaptorDIBuilder()


	override fun provide(type: KType, provide: RaptorDI.() -> Any?) {
		builder.provide(type = type, provide = provide)
	}


	override fun toString(): String =
		"DI configuration"


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		val module = builder.createModule("raptor")
		val factory = DefaultRaptorDI.Factory(modules = listOf(module))
		val di = factory.createDI<RaptorContext>(context = lazyContext)

		propertyRegistry.register(DIRaptorPropertyKey, di)
	}


	object Key : RaptorComponentKey2<RootDIRaptorComponent> {

		override fun toString() = "DI"
	}
}
