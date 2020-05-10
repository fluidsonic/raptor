package io.fluidsonic.raptor

import org.kodein.di.*


// FIXME consistent naming order for all components
internal class DefaultTransactionKodeinRaptorComponent : RaptorComponent.Base<DefaultTransactionKodeinRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()
	val factoryPropertyKey: RaptorPropertyKey<DefaultRaptorKodeinFactory> = FactoryPropertyKey()


	// FIXME allow components to subscribe to finalization? instead
	fun finalize(propertyRegistry: RaptorPropertyRegistry) {
		val configurations = configurations.toList()

		propertyRegistry.register(factoryPropertyKey, DefaultRaptorKodeinFactory(Kodein.Module("transaction") { // FIXME diff. name for other components
			for (configuration in configurations)
				configuration()
		}))
	}


	override fun toString() = "kodein configuration"


	object Key : RaptorComponentKey<DefaultTransactionKodeinRaptorComponent> {

		override fun toString() = "kodein"
	}


	private class FactoryPropertyKey : RaptorPropertyKey<DefaultRaptorKodeinFactory> {

		override fun toString() = "kodein factory (transaction)"
	}
}
