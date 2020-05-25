package io.fluidsonic.raptor

import freemarker.template.*


object FreemarkerRaptorFeature : RaptorFeature {

	override val id = raptorFreemarkerFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(FreemarkerRaptorComponent.Key, FreemarkerRaptorComponent())
	}
}


const val raptorFreemarkerFeatureId: RaptorFeatureId = "raptor.freemarker"


val RaptorContext.freemarker: Configuration
	get() = properties[FreemarkerRaptorPropertyKey]
		?: error("You must install ${FreemarkerRaptorFeature::class.simpleName} for enabling Freemarker functionality.")
