package io.fluidsonic.raptor

import io.fluidsonic.raptor.v2.*


@Raptor.Dsl3
interface Raptor {

	val context: RaptorContext
	val state: State

	suspend fun start()
	suspend fun stop()


	@DslMarker
	@Retention(AnnotationRetention.SOURCE)
	@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
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
fun raptor(configure: RaptorCoreComponent.() -> Unit): Raptor {
	val componentRegistry = DefaultRaptorComponentRegistry()
	val coreComponent = RaptorCoreComponent(registry = componentRegistry)

	componentRegistry.register(coreComponent, configure = configure)

	return DefaultRaptor(config = coreComponent.complete(componentRegistry = componentRegistry))
}
