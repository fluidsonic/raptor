package tests

import io.fluidsonic.raptor.*


interface TaggableComponent : RaptorComponent


@RaptorDsl
fun <Component : TaggableComponent> RaptorComponentSet<Component>.tags(vararg tags: Any) = configure {
	extensions.getOrSet(TagsRaptorComponentExtensionKey, ::mutableSetOf).addAll(tags)
}


@RaptorDsl
fun <Component : TaggableComponent> RaptorComponentSet<Component>.withTags(vararg tags: Any): RaptorComponentSet<Component> =
	withComponentAuthoring {
		filter { component ->
			// https://youtrack.jetbrains.com/issue/KT-38835
			component.extensions[TagsRaptorComponentExtensionKey]?.any { tags.contains(it) } ?: false
		}
	}


@RaptorDsl
fun <Component : TaggableComponent> RaptorComponentSet<Component>.withTags(vararg tags: Any, action: Component.() -> Unit) {
	withTags(*tags).configure(action)
}
