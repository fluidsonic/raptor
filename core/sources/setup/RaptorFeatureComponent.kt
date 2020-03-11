package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
interface RaptorFeatureComponent : RaptorComponent {

	val raptorComponentRegistry: RaptorComponentRegistry


	@Raptor.Dsl3
	fun install(feature: RaptorFeature)

	@Raptor.Dsl3
	fun kodein(config: Kodein.Builder.() -> Unit)

	@Raptor.Dsl3
	fun onStart(callback: suspend RaptorScope.() -> Unit)

	@Raptor.Dsl3
	fun onStop(callback: suspend RaptorScope.() -> Unit)
}


// FIXME move to relevant file
val RaptorFeatureComponent.transactions
	get(): RaptorConfigurableCollection<RaptorTransactionComponent> =
		TODO()
