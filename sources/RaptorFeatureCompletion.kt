package io.fluidsonic.raptor

import org.kodein.di.*


class RaptorFeatureCompletion internal constructor(
	context: RaptorSetupContext,
	private val kodeinBuilder: Kodein.Builder,
	lifecycleScope: RaptorSetupScope.LifecycleScope
) :
	RaptorSetupScope.KodeinScope,
	RaptorSetupScope.LifecycleScope by lifecycleScope {

	private val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	private val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()

	val raptorSetupContext = context


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinBuilder.apply(config)
	}


	override fun onStart(callback: suspend RaptorScope.() -> Unit) {
		startCallbacks += callback
	}


	override fun onStop(callback: suspend RaptorScope.() -> Unit) {
		stopCallbacks += callback
	}
}
