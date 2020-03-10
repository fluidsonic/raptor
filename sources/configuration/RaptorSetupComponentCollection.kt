package io.fluidsonic.raptor


interface RaptorSetupComponentCollection<out Component : Any> {

	fun all(config: Component.() -> Unit)
	fun filter(predicate: (registration: RaptorSetupRegistration<Component>) -> Boolean): RaptorSetupComponentCollection<Component>

	operator fun invoke(config: Component.() -> Unit) = all(config)
}


@Suppress("NAME_SHADOWING")
fun <Element : RaptorSetupElement.Taggable> RaptorSetupComponentCollection<Element>.withTag(
	tag: Any,
	config: Element.() -> Unit
) {
	filter { it.tags.contains(tag) }.all(config)
}


@Suppress("NAME_SHADOWING")
fun <Element : RaptorSetupElement.Taggable> RaptorSetupComponentCollection<Element>.withTags(
	vararg tags: Any
): RaptorSetupComponentCollection<Element> {
	val tags = tags.toList()

	return filter { it.tags.containsAll(tags) }
}
