package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorConfigurable<Component : RaptorComponent> {

	val raptorSetupContext: RaptorSetupContext

	fun forEachComponent(config: Component.() -> Unit)
}


@Raptor.Dsl3
operator fun <Configurable : RaptorConfigurable<*>> Configurable.invoke(config: Configurable.() -> Unit) {
	config()
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable, Configurable : RaptorConfigurable<Component>> Configurable.withTag(
	tag: Any
): Configurable {
	TODO()
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable, Configurable : RaptorConfigurable<Component>> Configurable.withTag(
	tag: Any,
	config: Configurable.() -> Unit
) {
	withTags(tag, config = config)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable, Configurable : RaptorConfigurable<Component>> Configurable.withTags(
	vararg tags: Any
): Configurable {
	TODO()
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable, Configurable : RaptorConfigurable<Component>> Configurable.withTags(
	vararg tags: Any,
	config: Configurable.() -> Unit
) {
	TODO()
}
