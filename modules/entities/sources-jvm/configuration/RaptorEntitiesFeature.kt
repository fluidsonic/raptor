package io.fluidsonic.raptor


public object RaptorEntitiesFeature : RaptorFeature.Configurable<RaptorEntitiesComponent> {

	override val id: RaptorFeatureId = raptorEntitiesFeatureId


	override fun RaptorTopLevelConfigurationScope.configure(action: RaptorEntitiesComponent.() -> Unit) {
		componentRegistry.configure(key = RaptorEntitiesComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(RaptorEntitiesComponent.Key, RaptorEntitiesComponent())
	}


	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
		val definitions = componentRegistry.one(RaptorEntitiesComponent.Key).complete()

		ifInstalled(raptorBsonFeatureId) {
			bson {
				definitions(definitions.map { it.bsonDefinition })
				definitions(RaptorTypedEntityId.bsonDefinition(definitions = definitions))
			}
		}

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { RaptorAnyEntityResolver(definitions = definitions, context = get()) }
			}
		}

		ifInstalled(raptorKtorFeatureId) {
			// ifInstalled(raptorGraphFeatureId) { FIXME make compileOnly
			graphs.definitions(definitions.map { it.graphDefinition() })
			graphs.definitions(
				RaptorEntity.graphDefinition(),
				RaptorEntityId.graphDefinition()
			)
			// }
		}

		ifInstalled(raptorTransactionFeatureId) {
			transactions.di {
				provide { RaptorAnyEntityResolver(definitions = definitions, context = get()) }
			}
		}
	}
}


public const val raptorEntitiesFeatureId: RaptorFeatureId = "io.fluidsonic.raptor.entities" // FIXME unify


@RaptorDsl
public val RaptorTopLevelConfigurationScope.entities: RaptorComponentSet<RaptorEntitiesComponent>
	get() = componentRegistry.configure(RaptorEntitiesComponent.Key)
