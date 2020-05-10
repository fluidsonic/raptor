package io.fluidsonic.raptor


object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		install(RaptorConfigurationFeature)
		install(RaptorLifecycleFeature)
		install(RaptorTransactionFeature)

		componentRegistry.register(KtorRaptorComponent.Key, KtorRaptorComponent(globalScope = this))

		// HoconApplicationConfig(ConfigFactory.defaultApplication().resolve()!!) // FIXME

		// FIXME prevent multi-start
		lifecycle {
			onStart {
				checkNotNull(context[Ktor.PropertyKey]).start()
			}

			onStop {
				checkNotNull(context[Ktor.PropertyKey]).stop()
			}
		}
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.ktor: RaptorComponentSet<KtorRaptorComponent>
	get() = componentRegistry.configure(KtorRaptorComponent.Key)
