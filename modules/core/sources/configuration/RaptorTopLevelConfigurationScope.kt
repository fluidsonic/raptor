package io.fluidsonic.raptor


@RaptorDsl
interface RaptorTopLevelConfigurationScope : RaptorFeatureInstallCheckScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry


	@RaptorDsl
	fun install(feature: RaptorFeature)


	@RaptorDsl
	fun <Feature : RaptorFeature.Configurable<ConfigurationScope>, ConfigurationScope : Any> install(
		feature: Feature,
		configuration: ConfigurationScope.() -> Unit
	)
}
