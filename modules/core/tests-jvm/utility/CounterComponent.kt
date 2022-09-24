package tests

import io.fluidsonic.raptor.*


class CounterComponent : RaptorComponent.Base<CounterComponent>() {

	private var count = 0


	@RaptorDsl
	fun increment() {
		count += 1
	}


	override fun toString() =
		"counter ($count)"


	override fun RaptorComponentConfigurationEndScope<CounterComponent>.onConfigurationEnded() {
		propertyRegistry.register(countPropertyKey, count)
	}


	companion object {

		val key = RaptorComponentKey<CounterComponent>("counter")
	}
}


@RaptorDsl
fun RaptorAssemblyQuery<CounterComponent>.increment() {
	this {
		increment()
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.counter
	get() = componentRegistry.all(CounterComponent.key)
