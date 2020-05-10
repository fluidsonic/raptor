package io.fluidsonic.raptor


@RaptorDsl
interface RaptorFeatureConfigurationEndScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry

	@RaptorDsl
	val lazyContext: RaptorContext

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry
}
