package io.fluidsonic.raptor


public interface RaptorTransactionConfigurationScope {

	@RaptorDsl
	public val lazyContext: RaptorTransactionContext

	@RaptorDsl
	public val parentContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry
}
