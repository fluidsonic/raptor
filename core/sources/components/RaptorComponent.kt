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


	interface TransactionBoundary<out Transaction : RaptorTransaction> : KodeinBoundary {

		@Raptor.Dsl3
		val transactions: RaptorComponentConfig<RaptorTransactionComponent>
	}
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentConfig<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentConfig<Component>.withTag(
	tag: Any,
	configure: Component.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentConfig<Component>.withTags(
	vararg tags: Any
): RaptorComponentConfig<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toHashSet()

	return RaptorComponentConfig.filter(this) { it.raptorTags.containsAll(tags) }
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentConfig<Component>.withTags(
	vararg tags: Any,
	configure: Component.() -> Unit
) {
	withTags(*tags).invoke(configure)
}
