package io.fluidsonic.raptor

import freemarker.template.*
import io.fluidsonic.raptor.di.*


public object FreemarkerRaptorFeature : RaptorFeature { // FIXME rn

	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(FreemarkerRaptorComponent.Key, FreemarkerRaptorComponent())

		ifFeature(RaptorDIFeature) {
			di {
				provide { get<RaptorContext>().freemarker }
			}
		}
	}
}


public val RaptorContext.freemarker: Configuration
	get() = properties[FreemarkerRaptorPropertyKey]
		?: error("You must install ${FreemarkerRaptorFeature::class.simpleName} for enabling Freemarker functionality.")
