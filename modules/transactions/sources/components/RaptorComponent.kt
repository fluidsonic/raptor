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
		val transactions: RaptorComponentSet<RaptorTransactionComponent>
	}
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTag(
	tag: Any,
	configure: Component.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTags(
	vararg tags: Any
): RaptorComponentSet<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toList()

	return RaptorComponentSet.new {
		if (raptorTags.containsAll(tags))
			it()
	}
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> Component.withTags(
	vararg tags: Any,
	configure: Component.() -> Unit
) {
	withTags(*tags).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTag(
	tag: Any
) =
	withTags(tag)


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTag(
	tag: Any,
	configure: Component.() -> Unit
) {
	withTag(tag).invoke(configure)
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTags(
	vararg tags: Any
): RaptorComponentSet<Component> {
	@Suppress("NAME_SHADOWING")
	val tags = tags.toList()

	return RaptorComponentSet.filter(this) { it.raptorTags.containsAll(tags) }
}


@Raptor.Dsl3
fun <Component : RaptorComponent.Taggable> RaptorComponentSet<Component>.withTags(
	vararg tags: Any,
	configure: Component.() -> Unit
) {
	withTags(*tags).invoke(configure)
}


fun <X> c() where X : RaptorComponent, X : RaptorComponentSet<RaptorComponent> {

}
