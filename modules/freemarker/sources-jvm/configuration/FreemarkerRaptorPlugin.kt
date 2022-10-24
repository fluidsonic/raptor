package io.fluidsonic.raptor

import freemarker.template.*
import io.fluidsonic.raptor.di.*


private val freemarkerComponentKey = RaptorComponentKey<FreemarkerRaptorComponent>("freemarker")
private val freemarkerPropertyKey = RaptorPropertyKey<Configuration>("freemarker configuration")


public object FreemarkerRaptorPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(freemarkerComponentKey, FreemarkerRaptorComponent())

		optional(RaptorDIPlugin) {
			di {
				provide<Configuration> { get<RaptorContext>().freemarker }
			}
		}
	}
}


public val RaptorContext.freemarker: Configuration
	get() = properties[freemarkerPropertyKey] ?: throw RaptorPluginNotInstalledException(FreemarkerRaptorPlugin)


internal fun RaptorPropertyRegistry.register(freemarker: Configuration) {
	register(freemarkerPropertyKey, freemarker)
}


@RaptorDsl
public val RaptorAssemblyScope.freemarker: FreemarkerRaptorComponent
	get() = componentRegistry.oneOrNull(freemarkerComponentKey) ?: throw RaptorPluginNotInstalledException(FreemarkerRaptorPlugin)
