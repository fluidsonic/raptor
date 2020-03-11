package io.fluidsonic.raptor


class KtorRaptorComponent internal constructor(
	internal val featureComponent: RaptorFeatureComponent
) : RaptorComponent {

	internal val serverComponents: MutableList<KtorServerRaptorComponent> = mutableListOf()


	internal fun complete() = KtorConfig(
		servers = serverComponents.map { it.complete() }
	)
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorRaptorComponent>.newServer(
	vararg tags: Any = emptyArray(),
	configure: RaptorConfigurable<KtorServerRaptorComponent>.() -> Unit = {}
) {
	val registry = raptorComponentRegistry

	raptorComponentConfiguration {
		val component = KtorServerRaptorComponent(
			featureComponent = featureComponent,
			raptorTags = tags.toSet()
		)
		serverComponents += component

		registry.register(component, configure = configure)
	}
}


@Raptor.Dsl3
val RaptorConfigurable<KtorRaptorComponent>.servers
	get() = raptorComponentRegistry.configureAll<KtorServerRaptorComponent>()
