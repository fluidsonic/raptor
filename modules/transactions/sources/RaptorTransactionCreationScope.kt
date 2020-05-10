package io.fluidsonic.raptor


interface RaptorTransactionCreationScope {

	@RaptorDsl
	val context: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorTransactionPropertyRegistry
}
