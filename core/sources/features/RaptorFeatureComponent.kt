package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
open class RaptorFeatureComponent internal constructor() : RaptorComponent.TransactionBoundary<RaptorTransaction> {

	internal val features: MutableSet<RaptorFeature> = mutableSetOf() // FIXME confusing - why doesn't each have its own RaptorFeatureComponent?
	internal val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()
	internal val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	internal val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()


	@Raptor.Dsl3
	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<RaptorFeatureComponent>.install(feature: RaptorFeature) {
	raptorComponentSelection {
		if (component.features.add(feature))
			with(feature) {
				this@install.setup()
			}
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<RaptorFeatureComponent>.onStart(callback: suspend RaptorScope.() -> Unit) {
	raptorComponentSelection {
		component.startCallbacks += callback
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<RaptorFeatureComponent>.onStop(callback: suspend RaptorScope.() -> Unit) {
	raptorComponentSelection {
		component.stopCallbacks += callback
	}
}
