package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


// FIXME name order
public object BsonRaptorFeature : RaptorFeature.Configurable<BsonRaptorComponent> {

	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: BsonRaptorComponent.() -> Unit) {
		componentRegistry.configure(key = BsonRaptorComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(BsonRaptorComponent.Key, BsonRaptorComponent())

		ifFeature(RaptorDIFeature) {
			di {
				provide { get<RaptorBsonProperties>().codecRegistry }
				provide { get<RaptorBsonProperties>().scope }
				provide { get<RaptorContext>().bson }
			}
		}
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
