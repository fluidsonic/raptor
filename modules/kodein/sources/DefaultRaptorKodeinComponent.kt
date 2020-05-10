package io.fluidsonic.raptor

import org.kodein.di.*


internal class DefaultRaptorKodeinComponent : RaptorComponent.Base<DefaultRaptorKodeinComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()


	fun finalize(): Kodein.Module {
		val configurations = configurations.toList()

		return Kodein.Module("raptor") {
			for (configuration in configurations)
				configuration()
		}
	}


	override fun toString() = "kodein configuration"


	object Key : RaptorComponentKey<DefaultRaptorKodeinComponent> {

		override fun toString() = "kodein"
	}
}
