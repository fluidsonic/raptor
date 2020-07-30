package io.fluidsonic.raptor

import org.kodein.di.*


internal class KodeinFactoryRaptorComponent : RaptorComponent.Default<KodeinFactoryRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()


	fun toFactory(name: String): RaptorKodeinFactory {
		val configurations = configurations.toList()

		return DefaultRaptorKodeinFactory(Kodein.Module(name) { // FIXME diff. name for other components
			for (configuration in configurations)
				configuration()
		})
	}


	override fun toString() = "kodein configuration"


	object Key : RaptorComponentKey<KodeinFactoryRaptorComponent> {

		override fun toString() = "kodein"
	}
}
