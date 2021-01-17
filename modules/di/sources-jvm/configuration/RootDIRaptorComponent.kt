package io.fluidsonic.raptor


internal class RootDIRaptorComponent : RaptorComponent.Default<RootDIRaptorComponent>() {

	val builder = DefaultRaptorDIBuilder()


	override fun toString() = "DI configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val module = builder.createModule("raptor")
		val factory = DefaultRaptorDI.Factory(modules = listOf(module))
		val di = factory.createDI(context = lazyContext)

		propertyRegistry.register(DIRaptorPropertyKey, di)
	}


	object Key : RaptorComponentKey<RootDIRaptorComponent> {

		override fun toString() = "di"
	}
}
