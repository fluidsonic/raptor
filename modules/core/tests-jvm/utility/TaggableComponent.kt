package tests

import io.fluidsonic.raptor.*


interface TaggableComponent<Component : TaggableComponent<Component>> : RaptorComponent<Component>


@RaptorDsl
fun <Component : TaggableComponent<out Component>> RaptorAssemblyQuery<Component>.tags(vararg tags: Any) {
	this {
		extensions.getOrSet(tagsComponentExtensionKey, ::mutableSetOf).addAll(tags)
	}
}


@RaptorDsl
fun <Component : TaggableComponent<out Component>> RaptorAssemblyQuery<Component>.withTags(vararg tags: Any): RaptorAssemblyQuery<Component> =
	filter { component ->
		// https://youtrack.jetbrains.com/issue/KT-38835
		component.extensions[tagsComponentExtensionKey]?.any { tags.contains(it) } ?: false
	}


@RaptorDsl
fun <Component : TaggableComponent<out Component>> RaptorAssemblyQuery<Component>.withTags(vararg tags: Any, action: Component.() -> Unit) {
	withTags(*tags)(action)
}
