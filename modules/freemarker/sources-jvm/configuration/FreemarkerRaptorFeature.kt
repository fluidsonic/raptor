package io.fluidsonic.raptor

import freemarker.template.*
import io.fluidsonic.raptor.di.*


private val freemarkerComponentKey = RaptorComponentKey<FreemarkerRaptorComponent>("freemarker")
private val freemarkerPropertyKey = RaptorPropertyKey<Configuration>("freemarker configuration")


public object FreemarkerRaptorFeature : RaptorFeature { // FIXME rm

	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(freemarkerComponentKey, FreemarkerRaptorComponent())

		ifFeature(RaptorDIFeature) {
			di {
				provide { get<RaptorContext>().freemarker }
			}
		}
	}
}


public val RaptorContext.freemarker: Configuration
	get() = properties[freemarkerPropertyKey] ?: throw RaptorFeatureNotInstalledException(FreemarkerRaptorFeature)


internal fun RaptorPropertyRegistry.register(freemarker: Configuration) {
	register(freemarkerPropertyKey, freemarker)
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.freemarker: FreemarkerRaptorComponent
	get() = componentRegistry.oneOrNull(freemarkerComponentKey) ?: throw RaptorFeatureNotInstalledException(FreemarkerRaptorFeature)
