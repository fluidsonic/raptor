package io.fluidsonic.raptor

import kotlin.reflect.*


interface RaptorComponentRegistry {

	fun <Component : RaptorComponent> getAll(clazz: KClass<Component>): List<RaptorComponentRegistration<Component>>

	fun <Component : RaptorComponent> getSingle(clazz: KClass<Component>): RaptorComponentRegistration<Component>?

	fun <Component : RaptorComponent> getSingle(component: Component, clazz: KClass<Component>): RaptorComponentRegistration<Component>


	interface Mutable : RaptorComponentRegistry {

		fun <Component : RaptorComponent> configureAll(clazz: KClass<Component>): RaptorComponentScope.Collection<Component>

		fun <Component : RaptorComponent> configureSingle(clazz: KClass<Component>): RaptorComponentScope<Component>

		fun <Component : RaptorComponent> configureSingle(component: Component, clazz: KClass<Component>): RaptorComponentScope<Component>

		override fun <Component : RaptorComponent> getAll(clazz: KClass<Component>): List<RaptorComponentRegistration.Mutable<Component>>

		override fun <Component : RaptorComponent> getSingle(clazz: KClass<Component>): RaptorComponentRegistration.Mutable<Component>?

		override fun <Component : RaptorComponent> getSingle(component: Component, clazz: KClass<Component>): RaptorComponentRegistration.Mutable<Component>

		fun <Component : RaptorComponent> register(
			component: Component,
			clazz: KClass<Component>,
			definesScope: Boolean = false
		): RaptorComponentScope<Component>
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
	noinline configure: RaptorComponentScope<Component>.() -> Unit
) {
	configureAll(Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureAll(
	clazz: KClass<Component>,
	configure: RaptorComponentScope<Component>.() -> Unit
) {
	configureAll(clazz).invoke(configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle() =
	configureSingle(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle(
	noinline configure: RaptorComponentScope<Component>.() -> Unit
) {
	configureSingle(Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle(
	clazz: KClass<Component>,
	configure: RaptorComponentScope<Component>.() -> Unit
) {
	configureSingle(clazz).invoke(configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingle(
	component: Component
): RaptorComponentScope<Component> =
	configureSingle(component = component, clazz = Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	create: () -> Component
): RaptorComponentScope<Component> =
	if (getSingle<Component>() !== null) configureSingle()
	else register(create())


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	create: () -> Component,
	noinline configure: RaptorComponentScope<Component>.() -> Unit
) {
	if (getSingle<Component>() !== null) configureSingle(configure = configure)
	else register(create(), configure = configure)
}


inline fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	clazz: KClass<Component>,
	create: () -> Component
): RaptorComponentScope<Component> =
	if (getSingle(clazz) !== null) configureSingle(clazz = clazz)
	else register(component = create(), clazz = clazz)


inline fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.configureSingleOrCreate(
	clazz: KClass<Component>,
	create: () -> Component,
	noinline configure: RaptorComponentScope<Component>.() -> Unit
) {
	if (getSingle(clazz) !== null) configureSingle(clazz = clazz, configure = configure)
	else register(component = create(), clazz = clazz, configure = configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.register(
	component: Component,
	definesScope: Boolean = false
) =
	register(
		component = component,
		clazz = Component::class,
		definesScope = definesScope
	)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.Mutable.register(
	component: Component,
	definesScope: Boolean = false,
	noinline configure: RaptorComponentScope<Component>.() -> Unit
) {
	register(
		component = component,
		clazz = Component::class,
		definesScope = definesScope,
		configure = configure
	)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.Mutable.register(
	component: Component,
	clazz: KClass<Component>,
	definesScope: Boolean = false,
	configure: RaptorComponentScope<Component>.() -> Unit
) {
	register(
		component = component,
		clazz = clazz,
		definesScope = definesScope
	).invoke(configure)
}
