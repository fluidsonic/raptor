package io.fluidsonic.raptor

import kotlin.reflect.*


internal class RootDIRaptorComponent : RaptorComponent.Default<RaptorDIComponent>(), RaptorDIComponent {

	private val builder = DefaultRaptorDIBuilder()


	override fun provide(type: KType, provide: RaptorDI.() -> Any) {
		builder.provide(type = type, provide = provide)
	}


	override fun toString(): String =
		"DI configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val module = builder.createModule("raptor")
		val factory = DefaultRaptorDI.Factory(modules = listOf(module))
		val di = factory.createDI(context = lazyContext)

		propertyRegistry.register(DIRaptorPropertyKey, di)
	}


	object Key : RaptorComponentKey<RootDIRaptorComponent> {

		override fun toString() = "DI"
	}
}
