package tests

import io.fluidsonic.raptor.*


class CounterComponent : RaptorComponent.Default<CounterComponent>() {

	var _count = 0


	override fun toString() =
		"counter ($_count)"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(CountRaptorPropertyKey, _count)
	}


	object Key : RaptorComponentKey<CounterComponent> {

		override fun toString() = "counter"
	}
}


@RaptorDsl
fun RaptorComponentSet<CounterComponent>.increment() = configure {
	_count += 1
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.counter
	get() = componentRegistry.configure(CounterComponent.Key)
