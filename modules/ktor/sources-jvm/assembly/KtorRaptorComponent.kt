package io.fluidsonic.raptor


public class KtorRaptorComponent internal constructor(
	internal val globalScope: RaptorTopLevelConfigurationScope,
) : RaptorComponent.Default<KtorRaptorComponent>() {

	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val servers = componentRegistry.many(RaptorKtorServerComponent.Key).map { serverComponent ->
			with(serverComponent) {
				toServerConfigurations()
			}
		}

		propertyRegistry.register(RaptorKtorImpl.PropertyKey, RaptorKtorImpl(
			configuration = KtorConfiguration(servers = servers),
			context = lazyContext
		))
	}


	internal object Key : RaptorComponentKey<KtorRaptorComponent> {

		override fun toString() = "ktor"
	}
}


@RaptorDsl
public fun RaptorComponentSet<KtorRaptorComponent>.newServer(insecure: Boolean = false, configure: RaptorKtorServerComponent.() -> Unit = {}) {
	configure {
		RaptorKtorServerComponent(globalScope = globalScope, insecure = insecure)
			.also { componentRegistry.register(RaptorKtorServerComponent.Key, it) }
			.also(configure)
	}
}


@RaptorDsl
public val RaptorComponentSet<KtorRaptorComponent>.servers: RaptorComponentSet<RaptorKtorServerComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(RaptorKtorServerComponent.Key)
		}
	}
