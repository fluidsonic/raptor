package io.fluidsonic.raptor


public object RaptorEntitiesFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorEntitiesFeatureId


	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
		val resolverTypes = componentRegistry.oneOrNull(RaptorEntitiesComponent.Key)?.resolverTypes.orEmpty()

		// FIXME
		ifInstalled(raptorDIFeatureId) {
			di.provide<RaptorEntityResolver<RaptorEntity, RaptorEntityId>> {
				RaptorAnyEntityResolver(context = get(), resolverTypes = resolverTypes)
			}
		}

		ifInstalled(raptorKtorFeatureId) {
			// ifInstalled(raptorGraphFeatureId) { FIXME make compileOnly
			graphs.definitions(
				RaptorEntity.graphDefinition(),
//				RaptorEntityId.graphDefinition()
			)
			// }
		}
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.entities: RaptorComponentSet<RaptorEntitiesComponent>
	get() = componentRegistry.oneOrRegister(RaptorEntitiesComponent.Key, ::RaptorEntitiesComponent)


public const val raptorEntitiesFeatureId: RaptorFeatureId = "io.fluidsonic.raptor.entities" // FIXME unify
