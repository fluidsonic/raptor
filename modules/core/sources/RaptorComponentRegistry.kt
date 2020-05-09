package io.fluidsonic.raptor

import kotlin.reflect.*


interface RaptorComponentRegistry {

	fun <Component : RaptorComponent<Component>> all(type: KClass<Component>): RaptorComponentSet<Component>
	fun <Component : RaptorComponent<Component>> register(component: Component, type: KClass<Component>)


	companion object
}


inline fun <reified Component : RaptorComponent<Component>> RaptorComponentRegistry.all(): RaptorComponentSet<Component> =
	all(Component::class)


inline fun <reified Component : RaptorComponent<Component>> RaptorComponentRegistry.all(noinline configure: Component.() -> Unit) {
	all(type = Component::class, configure = configure)
}


fun <Component : RaptorComponent<Component>> RaptorComponentRegistry.all(type: KClass<Component>, configure: Component.() -> Unit) {
	all(type).forEach(configure)
}


inline fun <reified Component : RaptorComponent<Component>> RaptorComponentRegistry.register(component: Component) {
	register(component = component, type = Component::class)
}
