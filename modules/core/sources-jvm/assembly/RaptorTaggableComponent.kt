package io.fluidsonic.raptor


public interface RaptorTaggableComponent<Component : RaptorTaggableComponent<Component>> : RaptorComponent<Component>


@RaptorDsl
public fun <Component : RaptorTaggableComponent<Component>> Component.tag(vararg tags: Any) {
	if (tags.isNotEmpty())
		tagRegistryOrCreate().addTags(tags.toHashSet())
}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent<Component>> RaptorComponentSet<Component>.tagged(vararg tags: Any): RaptorAssemblyQuery<Component> =
	all.tagged(*tags)


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent<Component>> RaptorComponentSet<Component>.tagged(
	vararg tags: Any,
	configure: Component.() -> Unit,
) {
	tagged(*tags)(configure)
}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent<Component>> RaptorAssemblyQuery<Component>.tagged(vararg tags: Any): RaptorAssemblyQuery<Component> =
	when {
		tags.isEmpty() -> this
		else -> RaptorTagMatchingComponentQuery(source = this, tagsToMatch = tags.toHashSet())
	}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@RaptorDsl
public fun <Component : RaptorTaggableComponent<Component>> RaptorAssemblyQuery<Component>.tagged(
	vararg tags: Any,
	configure: Component.() -> Unit,
) {
	tagged(tags)(configure)
}


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
@kotlin.internal.LowPriorityInOverloadResolution
@JvmName("taggedSetQuery")
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public fun <Component : RaptorTaggableComponent<Component>> RaptorAssemblyQuery<RaptorComponentSet<Component>>.tagged(
	vararg tags: Any,
): RaptorAssemblyQuery<Component> =
	all.tagged(*tags)


// TODO Support multiple tags with OR combination. OR: tagged(A, B) - AND: tagged(A).tagged(B)
// FIXME Doesn't work because it conflicts with the RaptorComponentSet extension.
@kotlin.internal.LowPriorityInOverloadResolution
@JvmName("taggedSetQuery")
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public fun <Component : RaptorTaggableComponent<Component>> RaptorAssemblyQuery<RaptorComponentSet<Component>>.tagged(
	vararg tags: Any,
	configure: Component.() -> Unit,
) {
	tagged(*tags)(configure)
}


// TODO Use RaptorTaggableComponent.tags with RaptorComponentConfigurationEndScope context receiver.
@RaptorDsl
public fun RaptorComponentConfigurationEndScope<out RaptorTaggableComponent<*>>.tags(): Set<Any> =
	component.tagRegistry()?.tags.orEmpty()


// TODO Sucks until we have context receivers.
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun RaptorComponentConfigurationEndScope<*>.tags(component: RaptorTaggableComponent<*>): Set<Any> =
	component.tagRegistry()?.tags.orEmpty()
