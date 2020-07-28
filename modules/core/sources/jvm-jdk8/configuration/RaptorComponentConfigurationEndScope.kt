package io.fluidsonic.raptor


interface RaptorComponentConfigurationEndScope : RaptorConfigurationEndScope {

	@RaptorDsl
	val lazyContext: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry
}
