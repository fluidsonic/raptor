package io.fluidsonic.raptor


class KtorRaptorComponent internal constructor(
	private val globalComponent: RaptorFeatureComponent
) : RaptorComponent {

	private val componentRegistry = globalComponent.componentRegistry.createChild()
	private val serverComponents: MutableList<KtorServerRaptorComponent> = mutableListOf()


	internal fun complete(globalCompletion: RaptorFeatureSetupCompletion): KtorConfig? {
		if (serverComponents.isEmpty())
			return null

		val servers = serverComponents.map { serverComponent ->
			serverComponent.complete(globalCompletion = globalCompletion)
		}

		return KtorConfig(servers = servers)
	}


	@Raptor.Dsl3
	fun newServer(
		vararg tags: Any = emptyArray(),
		configure: KtorServerRaptorComponent.() -> Unit = {}
	) {
		val serverComponent = KtorServerRaptorComponent(
			globalComponent = globalComponent,
			parentComponentRegistry = componentRegistry,
			raptorTags = tags.toHashSet()
		)
		serverComponents += serverComponent

		componentRegistry.register(serverComponent, configure = configure)
	}


	@Raptor.Dsl3
	val servers: RaptorComponentConfig<KtorServerRaptorComponent> = componentRegistry.configureAll()
}
