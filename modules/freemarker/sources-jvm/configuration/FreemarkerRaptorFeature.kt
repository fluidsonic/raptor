package io.fluidsonic.raptor

import freemarker.template.*


public object FreemarkerRaptorFeature : RaptorFeature { // FIXME rn

	override val id: RaptorFeatureId = raptorFreemarkerFeatureId


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(FreemarkerRaptorComponent.Key, FreemarkerRaptorComponent())

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { get<RaptorContext>().freemarker }
			}
		}
	}
}


public const val raptorFreemarkerFeatureId: RaptorFeatureId = "raptor.freemarker"


public val RaptorContext.freemarker: Configuration
	get() = properties[FreemarkerRaptorPropertyKey]
		?: error("You must install ${FreemarkerRaptorFeature::class.simpleName} for enabling Freemarker functionality.")
