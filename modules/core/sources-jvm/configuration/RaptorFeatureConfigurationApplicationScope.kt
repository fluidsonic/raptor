package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorFeatureConfigurationApplicationScope : RaptorConfigurationEndScope {

	@RaptorDsl
	public val componentRegistry: RaptorComponentRegistry

	@RaptorDsl
	public val componentRegistry2: RaptorComponentRegistry2

	@RaptorDsl
	public val lazyContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry
}
