package tests

import io.fluidsonic.raptor.*


class DummyComponent(private val name: String) : RaptorComponent.Base<DummyComponent>() {

	override fun toString() =
		"dummy ($name)"


	object Key : RaptorComponentKey<DummyComponent> {

		override fun toString() = "dummy"
	}
}
