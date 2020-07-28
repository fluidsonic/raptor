package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


internal class RootKodeinRaptorComponent : RaptorComponent.Default<RootKodeinRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()


	override fun toString() = "kodein configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val configurations = configurations.toList()

		val kodeinModule = Kodein.Module("raptor") { // FIXME diff. name for other components
			bind<RaptorContext>() with instance(lazyContext)

			for (configuration in configurations)
				configuration()
		}

		val kodein = Kodein {
			import(kodeinModule, allowOverride = true) // FIXME add special facility for testing
		}

		propertyRegistry.register(DKodeinRaptorPropertyKey, kodein.direct)
		propertyRegistry.register(KodeinRaptorPropertyKey, kodein)
	}


	object Key : RaptorComponentKey<RootKodeinRaptorComponent> {

		override fun toString() = "kodein"
	}
}
