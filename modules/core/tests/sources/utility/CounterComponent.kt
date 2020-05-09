package tests

import io.fluidsonic.raptor.*


class CounterComponent : RaptorComponent.Base<CounterComponent>() {

	var _count = 0


	fun finalize() =
		_count


	override fun toString() =
		"counter ($_count)"


	object Key : RaptorComponentKey<CounterComponent> {

		override fun toString() = "counter"
	}
}


@RaptorDsl
fun RaptorComponentSet<CounterComponent>.increment() = configure {
	_count += 1
}


@RaptorDsl
val RaptorGlobalConfigurationScope.counter
	get() = componentRegistry.configure(CounterComponent.Key)
