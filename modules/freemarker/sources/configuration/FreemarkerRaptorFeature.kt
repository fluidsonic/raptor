package io.fluidsonic.raptor

import freemarker.template.*


object FreemarkerRaptorFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(FreemarkerRaptorComponent.Key, FreemarkerRaptorComponent())
	}
}


val RaptorContext.freemarker: Configuration
	get() = properties[FreemarkerRaptorPropertyKey]
		?: error("You must install ${FreemarkerRaptorFeature::class.simpleName} for enabling Freemarker functionality.")
