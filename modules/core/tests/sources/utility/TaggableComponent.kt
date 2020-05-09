package tests

import io.fluidsonic.raptor.*


interface TaggableComponent : RaptorComponent


@RaptorDsl
fun <Component : TaggableComponent> RaptorComponentSet<Component>.tags(vararg tags: Any) = forEach {
	extensions.getOrSet(TagsRaptorExtensionKey, ::mutableSetOf).addAll(tags)
}


@RaptorDsl
fun <Component : TaggableComponent> RaptorComponentSet<Component>.withTags(vararg tags: Any): RaptorComponentSet<Component> =
	RaptorComponentSet.filter(this) { component ->
		component.extensions[TagsRaptorExtensionKey]?.any(tags::contains) ?: false
	}


@RaptorDsl
fun <Component : TaggableComponent> RaptorComponentSet<Component>.withTags(vararg tags: Any, action: Component.() -> Unit) {
	withTags(*tags).forEach(action)
}
