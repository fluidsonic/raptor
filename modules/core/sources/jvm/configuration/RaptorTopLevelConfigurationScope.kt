package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorTopLevelConfigurationScope : RaptorFeatureInstallCheckScope {

	@RaptorDsl
	public val componentRegistry: RaptorComponentRegistry


	@RaptorDsl
	public fun install(feature: RaptorFeature)


	@RaptorDsl
	public fun <Feature : RaptorFeature.Configurable<ConfigurationScope>, ConfigurationScope : Any> install(
		feature: Feature,
		configuration: ConfigurationScope.() -> Unit,
	)
}
