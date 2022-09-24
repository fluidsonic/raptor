package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


private val bsonComponentKey = RaptorComponentKey<RaptorBsonComponent>("bson")


public object RaptorBsonFeature : RaptorFeature.Configurable<RaptorBsonComponent> {

	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: RaptorBsonComponent.() -> Unit) {
		componentRegistry.one(bsonComponentKey).action()
	}


	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(bsonComponentKey, RaptorBsonComponent())

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
	get() = componentRegistry.oneOrNull(bsonComponentKey) ?: throw RaptorFeatureNotInstalledException(RaptorBsonFeature)
