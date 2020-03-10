package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*
import org.kodein.di.*


internal class RaptorFeatureSetupImpl(
	context: RaptorSetupContext,
	lifecycleScope: RaptorSetupScope.LifecycleScope // FIXME make hierarchical?
) : RaptorFeatureSetup,
	RaptorSetupScope.LifecycleScope by lifecycleScope {

	private val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()

	override val raptorSetupContext = context
	override val transactions = raptorSetupContext.collection<RaptorTransaction>()


	// FIXME duplicates
	override fun install(feature: RaptorFeature) {
		with(feature) {
			raptorSetupContext.configure<RaptorFeatureSetup> {
				setup()
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}
}
