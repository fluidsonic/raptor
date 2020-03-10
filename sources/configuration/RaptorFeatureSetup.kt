package io.fluidsonic.raptor


interface RaptorFeatureSetup :
	RaptorSetupElement,
	RaptorSetupScope.FeatureScope,
	RaptorSetupScope.KodeinScope,
	RaptorSetupScope.LifecycleScope {

	val transactions: RaptorSetupComponentCollection<RaptorTransaction>
}
