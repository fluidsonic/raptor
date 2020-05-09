package io.fluidsonic.raptor


interface RaptorFeatureFinalizationScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry


	companion object
}
