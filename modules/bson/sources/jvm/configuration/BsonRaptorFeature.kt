package io.fluidsonic.raptor


// FIXME name order
public object BsonRaptorFeature : RaptorFeature.Configurable<BsonRaptorComponent> {

	override val id: RaptorFeatureId = raptorBsonFeatureId


	override fun RaptorTopLevelConfigurationScope.configure(action: BsonRaptorComponent.() -> Unit) {
		componentRegistry.configure(key = BsonRaptorComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(BsonRaptorComponent.Key, BsonRaptorComponent())

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { get<RaptorContext>().bsonConfiguration }
				provide { DefaultBsonScope(configuration = get(), context = get()) }
				provide { get<BsonScope>().codecRegistry }
			}
		}
	}
}


public const val raptorBsonFeatureId: RaptorFeatureId = "raptor.bson"


public val RaptorContext.bsonConfiguration: BsonConfiguration
	get() = properties[BsonConfiguration.PropertyKey]
		?: error("You must install ${BsonRaptorFeature::class.simpleName} for enabling BSON functionality.")


@RaptorDsl
public val RaptorTopLevelConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
