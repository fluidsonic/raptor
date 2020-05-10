package io.fluidsonic.raptor


@RaptorDsl
interface RaptorFeatureFinalizationScope {

	@RaptorDsl
	val componentRegistry: RaptorComponentRegistry

	@RaptorDsl
	val propertyRegistry: RaptorPropertyRegistry


	@RaptorDsl
	fun onCompleted(action: CompletedScope.() -> Unit)


	companion object


	@RaptorDsl
	interface CompletedScope {

		@RaptorDsl
		val context: RaptorContext
	}
}
