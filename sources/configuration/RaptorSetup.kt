package io.fluidsonic.raptor


interface RaptorSetup : RaptorSetupElement,
	RaptorSetupScope.FeatureScope,
	RaptorSetupScope.KodeinScope,
	RaptorSetupScope.LifecycleScope {

	val transactions: RaptorSetupComponentCollection<RaptorTransaction>
}
