package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorConfigurable<out Component : RaptorComponent> {

	val raptorComponentRegistry: RaptorComponentRegistry

	fun raptorComponentConfiguration(configure: Component.() -> Unit)
	fun raptorComponentFilter(predicate: (Component) -> Boolean): RaptorConfigurable<Component>


	companion object {

		@Suppress("UNCHECKED_CAST")
		internal fun <Component : RaptorComponent> empty(registry: RaptorComponentRegistry) =
			Empty(registry = registry) as RaptorConfigurable<Component>
	}


	private class Empty(registry: RaptorComponentRegistry) : RaptorConfigurable<RaptorComponent> {

		override val raptorComponentRegistry = registry


		override fun raptorComponentConfiguration(configure: RaptorComponent.() -> Unit) = Unit
		override fun raptorComponentFilter(predicate: (RaptorComponent) -> Boolean) = this
	}
}


@Raptor.Dsl3
operator fun <Configurable : RaptorConfigurable<*>> Configurable.invoke(configure: Configurable.() -> Unit) {
	configure()
}
