package io.fluidsonic.raptor


// FIXME name order
object BsonRaptorFeature : RaptorFeature.WithRootComponent<BsonRaptorComponent> {

	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		// FIXME
//		kodein {
//			bind() from instance(config)
//
//			bind<BsonScope>() with singleton {
//				BsonScopeImpl(
//					config = instance(),
//					scope = instance()
//				)
//			}
//
//			bind<CodecRegistry>() with singleton {
//				instance<BsonScope>().codecRegistry
//			}
//		}
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(BsonRaptorComponent.Key, BsonRaptorComponent())
	}


	override val RaptorFeatureConfigurationStartScope.rootComponentKey: RaptorComponentKey<out BsonRaptorComponent>
		get() = BsonRaptorComponent.Key
}


val RaptorContext.bsonConfiguration: BsonConfiguration
	get() = properties[BsonConfiguration.PropertyKey]
		?: error("You must install ${BsonRaptorFeature::class.simpleName} for enabling BSON functionality.")


@RaptorDsl
val RaptorTopLevelConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
