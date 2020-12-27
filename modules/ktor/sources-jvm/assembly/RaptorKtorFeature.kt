package io.fluidsonic.raptor


public object RaptorKtorFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorKtorFeatureId


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		install(RaptorLifecycleFeature)
		install(RaptorTransactionFeature)

		componentRegistry.register(KtorRaptorComponent.Key, KtorRaptorComponent(globalScope = this))

		// FIXME prevent multi-start
		lifecycle {
			onStart {
				checkNotNull(context[RaptorKtorImpl.PropertyKey]).start()
			}

			onStop {
				checkNotNull(context[RaptorKtorImpl.PropertyKey]).stop()
			}
		}
	}
}


public const val raptorKtorFeatureId: RaptorFeatureId = "raptor.ktor"


@RaptorDsl
public val RaptorTopLevelConfigurationScope.ktor: RaptorComponentSet<KtorRaptorComponent>
	get() = componentRegistry.configure(KtorRaptorComponent.Key)
