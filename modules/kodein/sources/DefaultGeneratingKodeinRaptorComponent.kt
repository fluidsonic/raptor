package io.fluidsonic.raptor

import org.kodein.di.*


// FIXME consistent naming order for all components
internal class DefaultGeneratingKodeinRaptorComponent : RaptorComponent.Base<DefaultGeneratingKodeinRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()


	fun finalize(name: String): RaptorKodeinFactory {
		val configurations = configurations.toList()

		return DefaultRaptorKodeinFactory(Kodein.Module(name) { // FIXME diff. name for other components
			for (configuration in configurations)
				configuration()
		})
	}


	override fun toString() = "kodein configuration"


	object Key : RaptorComponentKey<DefaultGeneratingKodeinRaptorComponent> {

		override fun toString() = "kodein"
	}
}
