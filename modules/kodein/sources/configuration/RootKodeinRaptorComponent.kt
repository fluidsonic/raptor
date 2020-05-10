package io.fluidsonic.raptor

import org.kodein.di.*


internal class RootKodeinRaptorComponent : RaptorComponent.Default<RootKodeinRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()


	override fun toString() = "kodein configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val configurations = configurations.toList()

		val kodeinModule = Kodein.Module("raptor") { // FIXME diff. name for other components
			for (configuration in configurations)
				configuration()
		}

		propertyRegistry.register(KodeinRaptorPropertyKey, Kodein {
			import(kodeinModule, allowOverride = true) // FIXME add special facility for testing
		})
	}


	object Key : RaptorComponentKey<RootKodeinRaptorComponent> {

		override fun toString() = "kodein"
	}
}
