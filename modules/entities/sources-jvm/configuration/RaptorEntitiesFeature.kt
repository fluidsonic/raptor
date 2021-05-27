package io.fluidsonic.raptor


public object RaptorEntitiesFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorEntitiesFeatureId


	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
		// FIXME
//		ifInstalled(raptorDIFeatureId) {
//			di {
//				provide { RaptorAnyEntityResolver(definitions = definitions, context = get()) }
//			}
//		}

		ifInstalled(raptorKtorFeatureId) {
			// ifInstalled(raptorGraphFeatureId) { FIXME make compileOnly
			graphs.definitions(
				RaptorEntity.graphDefinition(),
//				RaptorEntityId.graphDefinition()
			)
			// }
		}

//		ifInstalled(raptorTransactionFeatureId) {
//			transactions.di {
//				provide { RaptorAnyEntityResolver(definitions = definitions, context = get()) }
//			}
//		}
	}
}


public const val raptorEntitiesFeatureId: RaptorFeatureId = "io.fluidsonic.raptor.entities" // FIXME unify
