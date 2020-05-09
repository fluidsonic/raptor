package io.fluidsonic.raptor

import kotlin.reflect.*


// FIXME Do we still need this?
interface RaptorComponentRegistry {

	fun <Component : RaptorComponent> getAll(clazz: KClass<Component>): List<RaptorComponentRegistration<Component>>

	fun <Component : RaptorComponent> getSingle(clazz: KClass<Component>): RaptorComponentRegistration<Component>?

	fun <Component : RaptorComponent> getSingle(component: Component, clazz: KClass<Component>): RaptorComponentRegistration<Component>


	interface Mutable : RaptorComponentRegistry {

		fun <Component : RaptorComponent> configureAll(clazz: KClass<Component>): RaptorComponentSet<Component>

		fun <Component : RaptorComponent> configureSingle(clazz: KClass<Component>): RaptorComponentSet<Component>

		fun <Component : RaptorComponent> configureSingle(component: Component, clazz: KClass<Component>): RaptorComponentSet<Component>

		fun createChild(): Mutable

		override fun <Component : RaptorComponent> getAll(clazz: KClass<Component>): List<RaptorComponentRegistration.Mutable<Component>>

		override fun <Component : RaptorComponent> getSingle(clazz: KClass<Component>): RaptorComponentRegistration.Mutable<Component>?

		override fun <Component : RaptorComponent> getSingle(component: Component, clazz: KClass<Component>): RaptorComponentRegistration.Mutable<Component>

		fun <Component : RaptorComponent> register(
			component: Component,
			clazz: KClass<Component>
		): RaptorComponentSet<Component>
	}
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.getAll(): List<RaptorComponentRegistration<Component>> =
	getAll(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.getAll(): List<RaptorComponentRegistration.Mutable<Component>> =
	getAll(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.getSingle(): RaptorComponentRegistration<Component>? =
	getSingle(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.getSingle(): RaptorComponentRegistration.Mutable<Component>? =
	getSingle(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.getSingle(
	component: Component
): RaptorComponentRegistration<Component> =
	getSingle(component = component, clazz = Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.getSingle(
	component: Component
): RaptorComponentRegistration.Mutable<Component> =
	getSingle(component = component, clazz = Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureAll() =
	configureAll(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureAll(
	noinline configure: Component.() -> Unit
) {
	configureAll(Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureAll(
	clazz: KClass<Component>,
	configure: Component.() -> Unit
) {
	configureAll(clazz).invoke(configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle() =
	configureSingle(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle(
	noinline configure: Component.() -> Unit
) {
	configureSingle(Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle(
	clazz: KClass<Component>,
	configure: Component.() -> Unit
) {
	configureSingle(clazz).invoke(configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle(
	component: Component
): RaptorComponentSet<Component> =
	configureSingle(component = component, clazz = Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	create: () -> Component
): RaptorComponentSet<Component> =
	if (getSingle<Component>() !== null) configureSingle()
	else register(create())


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	create: () -> Component,
	noinline configure: Component.() -> Unit
) {
	if (getSingle<Component>() !== null) configureSingle(configure = configure)
	else register(create(), configure = configure)
}


inline fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	clazz: KClass<Component>,
	create: () -> Component
): RaptorComponentSet<Component> =
	if (getSingle(clazz) !== null) configureSingle(clazz = clazz)
	else register(component = create(), clazz = clazz)


inline fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	clazz: KClass<Component>,
	create: () -> Component,
	noinline configure: Component.() -> Unit
) {
	if (getSingle(clazz) !== null) configureSingle(clazz = clazz, configure = configure)
	else register(component = create(), clazz = clazz, configure = configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.register(component: Component) =
	register(
		component = component,
		clazz = Component::class
	)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.register(
	component: Component,
	noinline configure: Component.() -> Unit
) {
	register(
		component = component,
		clazz = Component::class,
		configure = configure
	)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.register(
	component: Component,
	clazz: KClass<Component>,
	configure: Component.() -> Unit
) {
	register(
		component = component,
		clazz = clazz
	).invoke(configure)
}
