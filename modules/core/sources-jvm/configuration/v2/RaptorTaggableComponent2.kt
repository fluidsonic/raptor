package io.fluidsonic.raptor


public interface RaptorTaggableComponent2 : RaptorComponent2


@RaptorDsl
public fun RaptorTaggableComponent2.tag(vararg tags: Any) {
	if (tags.isNotEmpty())
		tagRegistryOrCreate().addTags(tags.toHashSet())
}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorComponentSet2<Component>.tagged(vararg tags: Any): RaptorAssemblyQuery2<Component> =
	all.tagged(*tags)


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorComponentSet2<Component>.tagged(
	vararg tags: Any,
	configure: Component.() -> Unit,
) {
	tagged(*tags)(configure)
}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorAssemblyQuery2<Component>.tagged(vararg tags: Any): RaptorAssemblyQuery2<Component> =
	when {
		tags.isEmpty() -> this
		else -> RaptorTagMatchingAssemblyQuery2(source = this, tagsToMatch = setOf(tags))
	}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorAssemblyQuery2<Component>.tagged(
	vararg tags: Any,
	configure: Component.() -> Unit,
) {
	tagged(tags)(configure)
}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@JvmName("taggedSetQuery")
@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorAssemblyQuery2<RaptorComponentSet2<Component>>.tagged(
	vararg tags: Any,
): RaptorAssemblyQuery2<Component> =
	all.tagged(*tags)


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@JvmName("taggedSetQuery")
@RaptorDsl
public fun <Component : RaptorTaggableComponent2> RaptorAssemblyQuery2<RaptorComponentSet2<Component>>.tagged(
	vararg tags: Any,
	configure: Component.() -> Unit,
) {
	tagged(*tags)(configure)
}


// TODO Use RaptorTaggableComponent.tags with RaptorComponentConfigurationEndScope context receiver.
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun RaptorComponentConfigurationEndScope2.tags(component: RaptorTaggableComponent2): Set<Any> =
	component.tagRegistry()?.tags.orEmpty()
