package io.fluidsonic.raptor


object RaptorConfigurationFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RaptorConfigurationComponent.Key, RaptorConfigurationComponent())
	}
}


val Raptor.configuration: RaptorConfiguration
	get() = context.configuration


val RaptorContext.configuration: RaptorConfiguration
	get() = properties[ConfigurationRaptorPropertyKey]
		?: error("You must install ${RaptorConfigurationFeature::class.simpleName} for enabling configuration functionality.")


@RaptorDsl
fun RaptorTopLevelConfigurationScope.configuration(configuration: RaptorConfiguration) {
	componentRegistry.configure(RaptorConfigurationComponent.Key) {
		configurations += configuration
	}
}
