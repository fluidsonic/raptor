package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


class RaptorCoreFeatureComponent internal constructor() : RaptorFeatureComponent() {

	internal fun complete(componentRegistry: RaptorComponentRegistry): RaptorConfig {
		val completion = RaptorFeatureSetupCompletion(componentRegistry = componentRegistry)
		for (feature in features)
			with(feature) {
				completion.completeSetup()
			}

		val transactionConfig = componentRegistry.getSingle<RaptorTransactionComponent>()?.component?.complete()

		return RaptorConfig(
			kodeinModule = Kodein.Module("raptor") {
				for (config in kodeinConfigs + completion.kodeinConfigs)
					config()

				if (transactionConfig != null)
					bind() from instance(transactionConfig)
			},
			startCallbacks = startCallbacks + completion.startCallbacks,
			stopCallbacks = stopCallbacks + completion.stopCallbacks
		)
	}
}
