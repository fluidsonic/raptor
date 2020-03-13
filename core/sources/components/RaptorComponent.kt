@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
interface RaptorComponent {

	interface KodeinBoundary : RaptorComponent {

		@Raptor.Dsl3
		fun kodein(configure: Kodein.Builder.() -> Unit)
	}


	interface Taggable : RaptorComponent {

		val raptorTags: Set<Any> // FIXME move to registration?
	}


	interface TransactionBoundary<out Transaction : RaptorTransaction> : KodeinBoundary
}


@Raptor.Dsl3
fun RaptorComponentScope<RaptorComponent.KodeinBoundary>.kodein(configure: Kodein.Builder.() -> Unit) { // FIXME make own API
	raptorComponentSelection {
		component.kodein(configure)
	}
}


@Raptor.Dsl3
val RaptorComponentScope<RaptorComponent.TransactionBoundary<*>>.transactions: RaptorComponentScope<RaptorTransactionComponent>
	get() = raptorComponentSelection.map {
		registry.configureSingleOrCreate(::RaptorTransactionComponent)
	}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope<Component>.withTag(
	tag: Any,
	configure: RaptorComponentScope<Component>.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope<Component>.withTags(
	vararg tags: Any
): RaptorComponentScope<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return raptorComponentSelection.filter { component.raptorTags.containsAll(tags) }
}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope<Component>.withTags(
	vararg tags: Any,
	configure: RaptorComponentScope<Component>.() -> Unit
) {
	withTags(*tags).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope.Collection<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope.Collection<Component>.withTag(
	tag: Any,
	configure: RaptorComponentScope.Collection<Component>.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope.Collection<Component>.withTags(
	vararg tags: Any
): RaptorComponentScope.Collection<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return raptorComponentSelection.filter { component.raptorTags.containsAll(tags) }
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentScope.Collection<Component>.withTags(
	vararg tags: Any,
	configure: RaptorComponentScope.Collection<Component>.() -> Unit
) {
	withTags(*tags).invoke(configure)
}
