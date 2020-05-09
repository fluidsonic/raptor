package io.fluidsonic.raptor

import kotlin.reflect.*


@RaptorDsl
interface RaptorFeatureFinalizationScope {

	@RaptorDsl
	fun <Value : Any> assign(key: RaptorKey<Value>, value: Value)

	@RaptorDsl
	fun <Component : RaptorComponent> components(type: KClass<Component>): Collection<Component>


	companion object
}
