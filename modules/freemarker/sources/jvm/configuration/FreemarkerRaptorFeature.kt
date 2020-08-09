package io.fluidsonic.raptor

import freemarker.template.*


object FreemarkerRaptorFeature : RaptorFeature { // FIXME rn

	override val id = raptorFreemarkerFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(FreemarkerRaptorComponent.Key, FreemarkerRaptorComponent())

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { get<RaptorContext>().freemarker }
			}
		}
	}
}


const val raptorFreemarkerFeatureId: RaptorFeatureId = "raptor.freemarker"


val RaptorContext.freemarker: Configuration
	get() = properties[FreemarkerRaptorPropertyKey]
		?: error("You must install ${FreemarkerRaptorFeature::class.simpleName} for enabling Freemarker functionality.")
