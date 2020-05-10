package io.fluidsonic.raptor


// FIXME name order
object BsonRaptorFeature : RaptorConfigurableFeature<BsonRaptorComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable() {
		propertyRegistry.register(BsonConfiguration.PropertyKey, componentRegistry.one(BsonRaptorComponent.Key).finalize())

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


	override fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponentKey<BsonRaptorComponent> =
		BsonRaptorComponent.Key.also {
			componentRegistry.register(it, BsonRaptorComponent())
		}
}


val RaptorContext.bsonConfiguration: BsonConfiguration
	get() = properties[BsonConfiguration.PropertyKey]
		?: error("You must install ${BsonRaptorFeature::class.simpleName} to access the BSON configuration.")


@RaptorDsl
val RaptorGlobalConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
