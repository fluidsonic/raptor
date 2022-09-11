package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


public object RaptorDIFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry2.register(RootDIRaptorComponent.Key, ::RootDIRaptorComponent)
	}


	override fun toString(): String = "DI"
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.di: RaptorDIComponent
	get() = componentRegistry2.oneOrNull(RootDIRaptorComponent.Key) ?: throw RaptorFeatureNotInstalledException(RaptorDIFeature)
