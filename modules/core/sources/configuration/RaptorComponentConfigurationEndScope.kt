package io.fluidsonic.raptor


interface RaptorComponentConfigurationEndScope {

	@RaptorDsl
	val lazyContext: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry
}
