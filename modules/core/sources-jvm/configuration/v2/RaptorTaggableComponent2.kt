package io.fluidsonic.raptor


public interface RaptorTaggableComponent2 : RaptorComponent2


@RaptorDsl
public fun RaptorTaggableComponent2.tag(vararg tags: Any) {
	if (tags.isNotEmpty())
		tagRegistryOrCreate().addTags(tags.toHashSet())
}


@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorComponentSet2<Component>.tagged(tag: Any): RaptorComponentSet2<Component> =
	RaptorTagMatchingComponentSet2(source = this, tagsToMatch = setOf(tag))


// TODO Use RaptorTaggableComponent.tags with RaptorComponentConfigurationEndScope context receiver.
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun RaptorComponentConfigurationEndScope2.tags(component: RaptorTaggableComponent2): Set<Any> =
	component.tagRegistry()?.tags.orEmpty()
