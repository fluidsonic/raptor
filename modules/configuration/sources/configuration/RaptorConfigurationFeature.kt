package io.fluidsonic.raptor

import org.kodein.di.erased.*


object RaptorConfigurationFeature : RaptorFeature {

	override val id = raptorConfigurationFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RaptorConfigurationComponent.Key, RaptorConfigurationComponent())

		ifInstalled(raptorKodeinFeatureId) {
			kodein {
				bind<RaptorConfiguration>() with singleton {
					instance<RaptorContext>().configuration
				}
			}
		}
	}
}


const val raptorConfigurationFeatureId: RaptorFeatureId = "raptor.configuration"


val Raptor.configuration: RaptorConfiguration
	get() = context.configuration


val RaptorContext.configuration: RaptorConfiguration
	get() = properties[ConfigurationRaptorPropertyKey]
		?: error("You must install ${RaptorConfigurationFeature::class.simpleName} for enabling configuration functionality.")


@RaptorDsl
val RaptorTopLevelConfigurationScope.configuration: RaptorComponentSet<RaptorConfigurationComponent>
	get() = componentRegistry.configure(RaptorConfigurationComponent.Key)
