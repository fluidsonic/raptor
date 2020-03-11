package io.fluidsonic.raptor

import org.kodein.di.*
import kotlin.reflect.*


@Raptor.Dsl3
class RaptorFeatureSetupCompletion internal constructor(
	private val context: RaptorComponentRegistry,
	private val kodeinBuilder: Kodein.Builder
) {

	private val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	private val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()


	inline fun <reified Component : RaptorComponent> component() =
		component(Component::class)


	fun <Component : RaptorComponent> component(clazz: KClass<Component>) =
		context.getSingle(clazz)


	@Raptor.Dsl3
	fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinBuilder.apply(config)
	}


	@Raptor.Dsl3
	fun onStart(callback: suspend RaptorScope.() -> Unit) {
		startCallbacks += callback
	}


	@Raptor.Dsl3
	fun onStop(callback: suspend RaptorScope.() -> Unit) {
		stopCallbacks += callback
	}
}
