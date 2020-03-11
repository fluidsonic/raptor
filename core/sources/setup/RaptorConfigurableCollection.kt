package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorConfigurableCollection<out Component : RaptorComponent> : RaptorConfigurable<Component> {

	override fun raptorComponentFilter(predicate: (Component) -> Boolean): RaptorConfigurableCollection<Component>
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTag(
	tag: Any,
	configure: RaptorConfigurableCollection<Component>.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTags(
	vararg tags: Any
): RaptorConfigurableCollection<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return raptorComponentFilter { it.raptorTags.containsAll(tags) }
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTags(
	vararg tags: Any,
	configure: RaptorConfigurableCollection<Component>.() -> Unit
) {
	withTags(*tags).invoke(configure)
}
