package io.fluidsonic.raptor


public object RaptorEntitiesFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorEntitiesFeatureId


	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
//		val definitions = componentRegistry.one(RaptorEntitiesComponent.Key).complete()
//
//		ifInstalled(raptorBsonFeatureId) {
//			bson {
//				definitions(definitions.map { it.bsonDefinition })
//				definitions(RaptorTypedEntityId.bsonDefinition(definitions = definitions))
//			}
//		}
//
//		ifInstalled(raptorDIFeatureId) {
//			di {
//				provide { RaptorAnyEntityResolver(definitions = definitions, context = get()) }
//			}
//		}
//
//		ifInstalled(raptorKtorFeatureId) {
//			// ifInstalled(raptorGraphFeatureId) { FIXME make compileOnly
//			graphs.definitions(definitions.map { it.graphDefinition() })
//			// FIXME
////			graphs.definitions(
////				RaptorEntity.graphDefinition(),
////				RaptorEntityId.graphDefinition()
////			)
//			// }
//		}
//
//		ifInstalled(raptorTransactionFeatureId) {
//			transactions.di {
//				provide { RaptorAnyEntityResolver(definitions = definitions, context = get()) }
//			}
//		}
	}
}


public const val raptorEntitiesFeatureId: RaptorFeatureId = "io.fluidsonic.raptor.entities" // FIXME unify
