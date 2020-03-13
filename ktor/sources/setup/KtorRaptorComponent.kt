package io.fluidsonic.raptor


class KtorRaptorComponent internal constructor(
	internal val globalFeatureSetup: RaptorFeatureSetup
) : RaptorComponent {

	internal val serverComponents: MutableList<KtorServerRaptorComponent> = mutableListOf()


	internal fun complete(globalCompletion: RaptorFeatureSetupCompletion): KtorConfig? {
		if (serverComponents.isEmpty())
			return null

		val servers = serverComponents.map { serverComponent ->
			serverComponent.complete(globalCompletion = globalCompletion)
		}

		return KtorConfig(servers = servers)
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorRaptorComponent>.newServer(
	vararg tags: Any = emptyArray(),
	configure: RaptorComponentScope<KtorServerRaptorComponent>.() -> Unit = {}
) {
	raptorComponentSelection {
		val serverComponent = KtorServerRaptorComponent(
			globalFeatureSetup = component.globalFeatureSetup,
			raptorTags = tags.toHashSet()
		)
		component.serverComponents += serverComponent

		registry.register(serverComponent, configure = configure, definesScope = true)
	}
}


@Raptor.Dsl3
val RaptorComponentScope<KtorRaptorComponent>.servers
	get() = raptorComponentSelection.map { registry.configureAll<KtorServerRaptorComponent>() }
