package io.fluidsonic.raptor


@Raptor.Dsl3
interface Raptor {

	val context: RaptorContext
	val state: State

	suspend fun start()
	suspend fun stop()


	@DslMarker
	@Retention(AnnotationRetention.SOURCE)
	annotation class Dsl3


	enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}


@Raptor.Dsl3
fun raptor(configure: RaptorSetup.() -> Unit): Raptor {
	val registry = RaptorComponentRegistry()
	val mainComponent = RaptorMainComponent()

	registry.register(mainComponent, configure = configure)

	return RaptorImpl(config = mainComponent.complete(registry = registry))
}
