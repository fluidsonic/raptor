package io.fluidsonic.raptor


interface RaptorTransactionConfigurationScope {

	@RaptorDsl
	val context: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry
}
