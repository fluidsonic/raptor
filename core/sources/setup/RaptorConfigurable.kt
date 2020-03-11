@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

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


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTag(
	tag: Any,
	configure: RaptorConfigurable<Component>.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTags(
	vararg tags: Any
): RaptorConfigurable<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return raptorComponentFilter { it.raptorTags.containsAll(tags) }
}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTags(
	vararg tags: Any,
	configure: RaptorConfigurable<Component>.() -> Unit
) {
	withTags(*tags).invoke(configure)
}
