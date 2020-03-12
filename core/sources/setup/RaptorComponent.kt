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

		val raptorTags: Set<Any>
	}


	interface TransactionBoundary<out Transaction : RaptorTransaction> : KodeinBoundary
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorComponent.KodeinBoundary>.kodein(configure: Kodein.Builder.() -> Unit) { // FIXME make own API
	raptorComponentConfiguration {
		kodein(configure)
	}
}


@Raptor.Dsl3
val RaptorConfigurable<RaptorComponent.TransactionBoundary<*>>.transactions: RaptorConfigurable<RaptorTransactionComponent>
	get() = raptorComponentRegistry.configureSingleOrCreate(::RaptorTransactionComponent)


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTag(
	tag: Any,
	configure: RaptorConfigurable<Component>.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTags(
	vararg tags: Any
): RaptorConfigurable<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return raptorComponentFilter { it.raptorTags.containsAll(tags) }
}


@kotlin.internal.LowPriorityInOverloadResolution
@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurable<Component>.withTags(
	vararg tags: Any,
	configure: RaptorConfigurable<Component>.() -> Unit
) {
	withTags(*tags).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTag(
	tag: Any,
	configure: RaptorConfigurableCollection<Component>.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTags(
	vararg tags: Any
): RaptorConfigurableCollection<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return raptorComponentFilter { it.raptorTags.containsAll(tags) }
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorConfigurableCollection<Component>.withTags(
	vararg tags: Any,
	configure: RaptorConfigurableCollection<Component>.() -> Unit
) {
	withTags(*tags).invoke(configure)
}
