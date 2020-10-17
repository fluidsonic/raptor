package io.fluidsonic.raptor


class KtorRaptorComponent internal constructor(
	internal val globalScope: RaptorTopLevelConfigurationScope,
) : RaptorComponent.Default<KtorRaptorComponent>() {

	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val servers = componentRegistry.many(KtorServerRaptorComponent.Key).map { serverComponent ->
			with(serverComponent) {
				toServerConfigurations()
			}
		}

		propertyRegistry.register(Ktor.PropertyKey, Ktor(
			configuration = KtorConfiguration(servers = servers),
			context = lazyContext
		))
	}


	internal object Key : RaptorComponentKey<KtorRaptorComponent> {

		override fun toString() = "ktor"
	}
}


@RaptorDsl
fun RaptorComponentSet<KtorRaptorComponent>.newServer(insecure: Boolean = false, configure: KtorServerRaptorComponent.() -> Unit = {}) {
	configure {
		KtorServerRaptorComponent(globalScope = globalScope, insecure = insecure)
			.also { componentRegistry.register(KtorServerRaptorComponent.Key, it) }
			.also(configure)
	}
}


@RaptorDsl
val RaptorComponentSet<KtorRaptorComponent>.servers: RaptorComponentSet<KtorServerRaptorComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(KtorServerRaptorComponent.Key)
		}
	}
