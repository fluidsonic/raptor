package tests

import io.fluidsonic.raptor.*


class StartableComponent : RaptorComponent.Base<StartableComponent>() {

	var delayInMilliseconds = 0L


	fun finalize() =
		Startable(delayInMilliseconds = delayInMilliseconds)


	override fun toString() = "startable"


	object Key : RaptorComponentKey<StartableComponent> {

		override fun toString() = "startable"
	}
}
