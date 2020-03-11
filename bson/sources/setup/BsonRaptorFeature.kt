package io.fluidsonic.raptor

import org.bson.codecs.configuration.*
import org.kodein.di.erased.*


object BsonRaptorFeature : RaptorFeature {

	override fun RaptorFeatureComponent.setup() {
		raptorComponentRegistry.register(BsonRaptorComponent())
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		val config = BsonConfig(
			definitions = component<BsonRaptorComponent>()?.definitions.orEmpty()
		)

		kodein {
			bind<BsonScope>() with singleton {
				BsonScopeImpl(
					config = config,
					scope = instance()
				)
			}

			bind<CodecRegistry>() with singleton {
				instance<BsonScope>().codecRegistry
			}
		}
	}
}


// FIXME is it okay to automatically register the feature?
@Raptor.Dsl3
val RaptorFeatureComponent.bson: RaptorConfigurable<BsonRaptorComponent>
	get() {
		install(BsonRaptorFeature) // FIXME check duplicates

		return raptorComponentRegistry.configureSingle()
	}
