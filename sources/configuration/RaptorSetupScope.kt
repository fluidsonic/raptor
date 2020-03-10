package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorSetupScope {

	interface FeatureScope : RaptorSetupScope {

		fun install(feature: RaptorFeature)
	}


	interface LifecycleScope : RaptorSetupScope {

		fun onStart(callback: suspend RaptorScope.() -> Unit)
		fun onStop(callback: suspend RaptorScope.() -> Unit)
	}


	interface KodeinScope : RaptorSetupScope {

		fun kodein(config: Kodein.Builder.() -> Unit) // FIXME wrap Kodein API?
	}
}
