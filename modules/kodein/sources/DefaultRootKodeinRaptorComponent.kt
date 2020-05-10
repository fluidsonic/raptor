package io.fluidsonic.raptor

import org.kodein.di.*


internal class DefaultRootKodeinRaptorComponent : RaptorComponent.Base<DefaultRootKodeinRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()
	val scopedComponents: MutableList<DefaultTransactionKodeinRaptorComponent> = mutableListOf() // FIXME remove


	// FIXME allow components to subscribe to finalization? instead
	fun finalize(propertyRegistry: RaptorPropertyRegistry) {
		val configurations = configurations.toList()

		val kodeinModule = Kodein.Module("raptor") { // FIXME diff. name for other components
			for (configuration in configurations)
				configuration()
		}

		propertyRegistry.register(DefaultKodeinRaptorPropertyKey, Kodein {
			import(kodeinModule, allowOverride = true) // FIXME add special facility for testing
		})

		for (component in scopedComponents)
			component.finalize(propertyRegistry = propertyRegistry)
	}


	override fun toString() = "kodein configuration"


	object Key : RaptorComponentKey<DefaultRootKodeinRaptorComponent> {

		override fun toString() = "kodein"
	}
}
