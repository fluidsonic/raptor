package io.fluidsonic.raptor

import kotlin.reflect.*


interface RaptorComponentRegistry {

	fun <Component : RaptorComponent> all(type: KClass<Component>): RaptorComponentSet<Component>

	fun createChildRegistry(): RaptorComponentRegistry

	fun <Component : RaptorComponent> register(component: Component, type: KClass<Component>)


	companion object
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.all(): RaptorComponentSet<Component> =
	all(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.all(noinline configure: Component.() -> Unit) {
	all(type = Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.all(type: KClass<Component>, configure: Component.() -> Unit) {
	all(type).forEach(configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.register(component: Component) {
	register(component = component, type = Component::class)
}
