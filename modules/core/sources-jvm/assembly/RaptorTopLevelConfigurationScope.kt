package io.fluidsonic.raptor


// FIXME rn to RaptorAssemblyScope?
@RaptorDsl
public interface RaptorTopLevelConfigurationScope : RaptorFeatureCheckScope {

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
