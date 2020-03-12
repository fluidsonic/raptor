package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorConfigurableCollection<out Component : RaptorComponent> : RaptorConfigurable<Component> {

	override fun raptorComponentFilter(predicate: (Component) -> Boolean): RaptorConfigurableCollection<Component>
}
