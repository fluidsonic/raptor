package io.fluidsonic.raptor


public interface RaptorComponentConfigurationEndScope2 : RaptorConfigurationEndScope {

	@RaptorDsl
	public val lazyContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry
}
