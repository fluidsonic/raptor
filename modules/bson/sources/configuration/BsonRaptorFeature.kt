package io.fluidsonic.raptor

import org.bson.codecs.configuration.*
import org.kodein.di.erased.*


// FIXME name order
object BsonRaptorFeature : RaptorFeature.WithRootComponent<BsonRaptorComponent> {

	override val id: RaptorFeatureId = raptorBsonFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(BsonRaptorComponent.Key, BsonRaptorComponent())

		ifInstalled(raptorKodeinFeatureId) {
			kodein {
				bind() from singleton {
					raptorContext.bsonConfiguration
				}

				bind<BsonScope>() with singleton {
					DefaultBsonScope(
						configuration = instance(),
						context = instance()
					)
				}

				bind<CodecRegistry>() with singleton {
					instance<BsonScope>().codecRegistry
				}
			}
		}
	}


	override val RaptorFeatureConfigurationStartScope.rootComponentKey: RaptorComponentKey<out BsonRaptorComponent>
		get() = BsonRaptorComponent.Key
}


const val raptorBsonFeatureId: RaptorFeatureId = "raptor.bson"


val RaptorContext.bsonConfiguration: BsonConfiguration
	get() = properties[BsonConfiguration.PropertyKey]
		?: error("You must install ${BsonRaptorFeature::class.simpleName} for enabling BSON functionality.")


@RaptorDsl
val RaptorTopLevelConfigurationScope.bson: RaptorComponentSet<BsonRaptorComponent>
	get() = componentRegistry.configure(BsonRaptorComponent.Key)
