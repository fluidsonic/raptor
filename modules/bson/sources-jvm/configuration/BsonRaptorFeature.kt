package io.fluidsonic.raptor


// FIXME name order
public object BsonRaptorFeature : RaptorFeature.Configurable<BsonRaptorComponent> {

	override val id: RaptorFeatureId = raptorBsonFeatureId


	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: BsonRaptorComponent.() -> Unit) {
		componentRegistry.configure(key = BsonRaptorComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(BsonRaptorComponent.Key, BsonRaptorComponent())

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { get<RaptorBsonProperties>().codecRegistry }
				provide { get<RaptorBsonProperties>().scope }
				provide { get<RaptorContext>().bson }
			}
		}
	}
}


public const val raptorBsonFeatureId: RaptorFeatureId = "raptor.bson"


@RaptorDsl
public val RaptorTopLevelConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
