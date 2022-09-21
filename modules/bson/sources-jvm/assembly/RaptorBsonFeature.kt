package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


public object RaptorBsonFeature : RaptorFeature.Configurable<RaptorBsonComponent> {

	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: RaptorBsonComponent.() -> Unit) {
		componentRegistry2.one(RaptorBsonComponent.Key).action()
	}


	override fun RaptorFeatureScope.installed() {
		componentRegistry2.register(RaptorBsonComponent.Key, RaptorBsonComponent())

		ifFeature(RaptorDIFeature) {
			di {
				provide { get<RaptorBson>().codecRegistry }
				provide { get<RaptorBson>().scope }
				provide { get<RaptorContext>().bson }
			}
		}
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.bson: RaptorBsonComponent
	get() = componentRegistry2.oneOrNull(RaptorBsonComponent.Key) ?: throw RaptorFeatureNotInstalledException(RaptorBsonFeature)
