package io.fluidsonic.raptor


interface RaptorTransactionConfigurationScope {

	@RaptorDsl
	val lazyContext: RaptorTransactionContext

	@RaptorDsl
	val parentContext: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry
}
