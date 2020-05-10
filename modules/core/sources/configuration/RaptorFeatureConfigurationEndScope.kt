package io.fluidsonic.raptor


@RaptorDsl
interface RaptorFeatureConfigurationEndScope : RaptorConfigurationEndScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry

	@RaptorDsl
	val lazyContext: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry
}
