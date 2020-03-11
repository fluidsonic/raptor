package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
interface RaptorFeatureSetup : RaptorComponent {

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
val RaptorFeatureSetup.transactions
	get(): RaptorConfigurableCollection<RaptorTransactionComponent> =
		raptorSetupContext.getOrCreateComponentCollection()
