package kodein

import io.fluidsonic.raptor.*

interface Taggable : RaptorComponent {

	val raptorTags: Set<Any> // FIXME move to registration?
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTag(
	tag: Any,
	configure: Component.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTags(
	vararg tags: Any
): RaptorComponentSet<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toList()

	return RaptorComponentSet.new {
		if (raptorTags.containsAll(tags))
			it()
	}
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTags(
	vararg tags: Any,
	configure: Component.() -> Unit
) {
	withTags(*tags).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTag(
	tag: Any,
	configure: Component.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTags(
	vararg tags: Any
): RaptorComponentSet<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toList()

	return RaptorComponentSet.filter(this) { it.raptorTags.containsAll(tags) }
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTags(
	vararg tags: Any,
	configure: Component.() -> Unit
) {
	withTags(*tags).invoke(configure)
}
