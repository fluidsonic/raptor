package io.fluidsonic.raptor


public interface RaptorTaggableComponent : RaptorComponent


@RaptorDsl
public fun <Component : RaptorTaggableComponent> RaptorComponentSet<Component>.addTags(vararg tags: Any) {
	if (tags.isNotEmpty())
		configure { tagRegistry.addTags(tags.toHashSet()) }
}


@RaptorDsl
public fun <Component : RaptorTaggableComponent> RaptorComponentSet<Component>.withTag(tag: Any): RaptorComponentSet<Component> =
	RaptorTagMatchingComponentSet(source = this, tagsToMatch = setOf(tag))


// https://youtrack.jetbrains.com/issue/KT-15708
@RaptorDsl
public fun <Component : RaptorTaggableComponent> RaptorComponentSet<Component>.withTag(tag: Any, action: Component.() -> Unit) {
	withTag(tag).configure(action)
}
