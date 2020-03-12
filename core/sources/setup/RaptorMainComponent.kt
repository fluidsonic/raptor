package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


class RaptorMainComponent internal constructor() : RaptorFeatureComponent() {

	internal fun complete(registry: RaptorComponentRegistry): RaptorConfig {
		val transactionConfig = registry.getSingle<RaptorTransactionComponent>()?.complete()

		return RaptorConfig(
			kodeinModule = Kodein.Module("raptor") {
				for (config in kodeinConfigs)
					config()

				if (transactionConfig != null)
					bind() from instance(transactionConfig)
			},
			startCallbacks = startCallbacks,
			stopCallbacks = stopCallbacks
		)
	}
}
