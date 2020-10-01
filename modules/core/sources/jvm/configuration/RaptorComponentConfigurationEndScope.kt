package io.fluidsonic.raptor


public interface RaptorComponentConfigurationEndScope : RaptorConfigurationEndScope {

	@RaptorDsl
	public val lazyContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry
}
