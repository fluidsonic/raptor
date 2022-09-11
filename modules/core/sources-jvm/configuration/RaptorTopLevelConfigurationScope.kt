package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorTopLevelConfigurationScope : RaptorFeatureTestScope {

	@RaptorDsl
	public val componentRegistry: RaptorComponentRegistry

	@RaptorDsl
	public val componentRegistry2: RaptorComponentRegistry2

	@RaptorDsl
	public fun install(feature: RaptorFeature)

	@RaptorDsl
	public fun <Feature : RaptorFeature.Configurable<ConfigurationScope>, ConfigurationScope : Any> install(
		feature: Feature,
		configuration: ConfigurationScope.() -> Unit,
	)
}
