package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
open class RaptorFeatureComponent internal constructor(
	val componentRegistry: RaptorComponentRegistry.Mutable
) : RaptorComponent.TransactionBoundary<RaptorTransaction> {

	internal val features: MutableSet<RaptorFeature> = mutableSetOf() // FIXME confusing - why doesn't each have its own RaptorFeatureComponent?
	internal val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()
	internal val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	internal val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()


	@Raptor.Dsl3
	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}


	@Raptor.Dsl3
	fun install(feature: RaptorFeature) {
		if (features.add(feature))
			with(feature) {
				setup()
			}
	}


	@Raptor.Dsl3
	fun onStart(callback: suspend RaptorScope.() -> Unit) {
		startCallbacks += callback
	}


	@Raptor.Dsl3
	fun onStop(callback: suspend RaptorScope.() -> Unit) {
		stopCallbacks += callback
	}


	override val transactions: RaptorComponentConfig<RaptorTransactionComponent> = componentRegistry.configureSingle()
}


typealias RaptorFeatureSetup = RaptorFeatureComponent // FIXME
