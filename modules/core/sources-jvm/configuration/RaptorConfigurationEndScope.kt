package io.fluidsonic.raptor


public interface RaptorConfigurationEndScope {

	@RaptorDsl
	public val lazyContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry

	@RaptorDsl
	public fun RaptorComponent2.endConfiguration()
}
