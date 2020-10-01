package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.internal.*


// FIXME name order
public object BsonRaptorFeature : RaptorFeature.Configurable<BsonRaptorComponent> {

	override val id: RaptorFeatureId = raptorBsonFeatureId


	override fun RaptorTopLevelConfigurationScope.configure(action: BsonRaptorComponent.() -> Unit) {
		componentRegistry.configure(key = BsonRaptorComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(BsonRaptorComponent.Key, BsonRaptorComponent())

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { get<RaptorContext>().bsonConfiguration }
				provide<RaptorBsonScope> { DefaultRaptorBsonScope(configuration = get(), context = get()) }
				provide { get<RaptorBsonScope>().codecRegistry }
				provide { get<RaptorBsonCodecRegistry>().internal() }
			}
		}
	}
}


public const val raptorBsonFeatureId: RaptorFeatureId = "raptor.bson"


public val RaptorContext.bsonConfiguration: RaptorBsonConfiguration
	get() = properties[RaptorBsonConfiguration.PropertyKey]
		?: error("You must install ${BsonRaptorFeature::class.simpleName} for enabling BSON functionality.")


@RaptorDsl
public val RaptorTopLevelConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
