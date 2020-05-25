package io.fluidsonic.raptor


@RaptorDsl
interface RaptorTopLevelConfigurationScope : RaptorFeatureInstallCheckScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry


	@RaptorDsl
	fun install(feature: RaptorFeature)


	@RaptorDsl
	fun <Feature : RaptorFeature.WithRootComponent<RootComponent>, RootComponent : RaptorComponent> install(
		feature: Feature,
		configuration: RootComponent.() -> Unit
	)
}
