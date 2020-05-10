package io.fluidsonic.raptor


object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		componentRegistry.one(KtorRaptorComponent.Key).finalize()
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(KtorRaptorComponent.Key, KtorRaptorComponent())

		// HoconApplicationConfig(ConfigFactory.defaultApplication().resolve()!!) // FIXME

		// FIXME prevent multi-start
		lifecycle {
			onStart {
				checkNotNull(context[KtorRaptorPropertyKey]).start()
			}

			onStop {
				checkNotNull(context[KtorRaptorPropertyKey]).stop()
			}
		}
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.ktor: RaptorComponentSet<KtorRaptorComponent>
	get() = componentRegistry.configure(KtorRaptorComponent.Key)
