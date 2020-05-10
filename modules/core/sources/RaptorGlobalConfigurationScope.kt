package io.fluidsonic.raptor


@RaptorDsl
interface RaptorGlobalConfigurationScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry


	@RaptorDsl
	fun <Feature : RaptorConfigurableFeature<RootComponent>, RootComponent : RaptorComponent> install(
		feature: Feature,
		configure: RootComponent.() -> Unit = {}
	)


	companion object
}
