package io.fluidsonic.raptor


internal class DIFactoryRaptorComponent : RaptorComponent.Default<DIFactoryRaptorComponent>() {

	val builder = DefaultRaptorDIBuilder()


	fun toFactory(name: String): RaptorDI.Factory =
		DefaultRaptorDI.Factory(modules = listOf(builder.createModule(name = name)))


	override fun toString() = "DI configuration"


	object Key : RaptorComponentKey<DIFactoryRaptorComponent> {

		override fun toString() = "DI"
	}
}
