package tests

import io.fluidsonic.raptor.*


class DummyComponent(private val name: String = "default") : RaptorComponent.Base<DummyComponent>() {

	override fun toString() =
		"dummy ($name)"


	object Key : RaptorComponentKey<DummyComponent> {

		override fun toString() = "dummy"
	}
}
