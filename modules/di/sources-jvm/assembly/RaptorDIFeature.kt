package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


private val rootComponentKey = RaptorComponentKey<RootDIRaptorComponent>("DI")


public object RaptorDIFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(rootComponentKey, ::RootDIRaptorComponent)
	}


	override fun toString(): String = "DI"
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.di: RaptorDIComponent<*>
	get() = componentRegistry.oneOrNull(rootComponentKey) ?: throw RaptorFeatureNotInstalledException(RaptorDIFeature)
