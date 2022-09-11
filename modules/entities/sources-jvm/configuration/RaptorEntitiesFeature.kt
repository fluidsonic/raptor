package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.ktor.*


public object RaptorEntitiesFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
		val resolverTypes = componentRegistry.oneOrNull(RaptorEntitiesComponent.Key)?.resolverTypes.orEmpty()

		// FIXME
		ifFeature(RaptorDIFeature) {
			di.provide<RaptorEntityResolver<RaptorEntity, RaptorEntityId>> {
				RaptorAnyEntityResolver(context = get(), resolverTypes = resolverTypes)
			}
		}

		ifFeature(RaptorKtorFeature) {
			// ifInstalled(raptorGraphFeatureId) { FIXME make compileOnly
			// FIXME
//			graphs.definitions(
//				RaptorEntity.graphDefinition(),
////				RaptorEntityId.graphDefinition()
//			)
			// }
		}
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.entities: RaptorComponentSet<RaptorEntitiesComponent>
	get() = componentRegistry.oneOrRegister(RaptorEntitiesComponent.Key, ::RaptorEntitiesComponent)
