package io.fluidsonic.raptor


class KtorRaptorComponent internal constructor() : RaptorComponent.Default<KtorRaptorComponent>() {

	// FIXME use global scoped finalization
	internal fun finalize(): KtorConfiguration? {
		val servers = componentRegistry.many(KtorServerRaptorComponent.Key)
			.ifEmpty { return null }
			.map { it.finalize() }

		return KtorConfiguration(servers = servers)
	}


	internal object Key : RaptorComponentKey<KtorRaptorComponent> {

		override fun toString() = "ktor"
	}
}


@RaptorDsl
fun RaptorComponentSet<KtorRaptorComponent>.newServer(configure: KtorServerRaptorComponent.() -> Unit = {}) {
	configure {
		KtorServerRaptorComponent()
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
