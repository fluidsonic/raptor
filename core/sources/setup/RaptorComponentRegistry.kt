package io.fluidsonic.raptor

import kotlin.reflect.*


// FIXME scopes
class RaptorComponentRegistry internal constructor() {

	private val registrationsByClass: MutableMap<KClass<out RaptorComponent>, RegistrationCollection<*>> = hashMapOf()


	fun <Component : RaptorComponent> configureAll(clazz: KClass<Component>): RaptorConfigurableCollection<Component> =
		getOrCreateCollection(clazz)


	fun <Component : RaptorComponent> configureSingle(clazz: KClass<Component>): RaptorConfigurable<Component> =
		getOrCreateCollection(clazz)
			.apply {
				if (size > 1)
					error("Cannot configure a single component of $clazz as $size have been registered:\n$this")
			}


	fun <Component : RaptorComponent> getAll(clazz: KClass<Component>): List<Component> =
		getCollection(clazz)?.components().orEmpty()


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent> getCollection(clazz: KClass<Component>) =
		registrationsByClass[clazz] as RegistrationCollection<Component>?


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent> getOrCreateCollection(clazz: KClass<Component>) =
		registrationsByClass.getOrPut(clazz) {
			RegistrationCollection<Component>(registry = this)
		} as RegistrationCollection<Component>


	fun <Component : RaptorComponent> getSingle(clazz: KClass<Component>): Component? =
		getCollection(clazz)
			?.apply {
				if (size > 1)
					error("Cannot get single component of $clazz as $size have been registered:\n$this")
			}
			?.firstOrNull()
			?.component


	fun <Component : RaptorComponent> register(
		component: Component,
		clazz: KClass<Component>
	): RaptorConfigurable<Component> {
		// FIXME handle multiple registrations of the same instance

		return getOrCreateCollection(clazz).addComponent(component = component)
	}


	private class Registration<out Component : RaptorComponent>(
		val component: Component,
		registry: RaptorComponentRegistry
	) : RaptorConfigurable<Component> {

		override val raptorComponentRegistry = registry


		override fun raptorComponentConfiguration(configure: Component.() -> Unit) {
			component.apply(configure)
		}


		override fun raptorComponentFilter(predicate: (Component) -> Boolean): RaptorConfigurable<Component> =
			if (predicate(component)) this
			else RaptorConfigurable.empty(registry = raptorComponentRegistry)
	}


	private class RegistrationCollection<Component : RaptorComponent>(
		registry: RaptorComponentRegistry
	) : RaptorConfigurableCollection<Component> {

		private val configurations = mutableListOf<Component.() -> Unit>()
		private val registrations = mutableListOf<Registration<Component>>()

		override val raptorComponentRegistry = registry


		fun addComponent(component: Component): Registration<Component> {
			val registration = Registration(component = component, registry = raptorComponentRegistry)
			registrations += registration

			val configurations = configurations

			with(component) {
				// We iterate over indices because the configurations we call may append more configurations.
				for (index in configurations.indices)
					configurations[index]()
			}

			return registration
		}


		fun addConfiguration(configuration: Component.() -> Unit) {
			configurations += configuration

			// We iterate over indices because the configuration we call may append more components.
			for (index in registrations.indices)
				registrations[index].component.apply(configuration)
		}


		fun components() =
			registrations.map { it.component }


		fun firstOrNull() =
			registrations.firstOrNull()


		override fun raptorComponentConfiguration(configure: (Component) -> Unit) {
			addConfiguration(configure)
		}


		override fun raptorComponentFilter(predicate: (Component) -> Boolean): RaptorConfigurableCollection<Component> =
			Filtered(predicate = predicate, source = this)


		val size
			get() = registrations.size


		class Filtered<Component : RaptorComponent>(
			private val predicate: (Component) -> Boolean,
			private val source: RaptorConfigurableCollection<Component>
		) : RaptorConfigurableCollection<Component> {

			override val raptorComponentRegistry
				get() = source.raptorComponentRegistry


			override fun raptorComponentFilter(predicate: (Component) -> Boolean): RaptorConfigurableCollection<Component> =
				Filtered(predicate = predicate, source = this)


			override fun raptorComponentConfiguration(configure: Component.() -> Unit) {
				source.raptorComponentConfiguration {
					if (this@Filtered.predicate(this))
						configure()
				}
			}
		}
	}
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.configureAll() =
	configureAll(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.configureAll(noinline configure: Component.() -> Unit) {
	configureAll(Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.configureAll(clazz: KClass<Component>, configure: Component.() -> Unit) {
	configureAll(clazz).invoke {
		raptorComponentConfiguration(configure)
	}
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.configureSingle() =
	configureSingle(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.configureSingle(
	noinline configure: RaptorConfigurable<Component>.() -> Unit
) {
	configureSingle(Component::class, configure = configure)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.configureSingle(
	clazz: KClass<Component>,
	configure: RaptorConfigurable<Component>.() -> Unit
) {
	configureSingle(clazz).invoke(configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.configureSingleOrCreate(
	create: () -> Component
): RaptorConfigurable<Component> =
	if (getSingle<Component>() !== null) configureSingle()
	else register(create())


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.configureSingleOrCreate(
	create: () -> Component,
	noinline configure: RaptorConfigurable<Component>.() -> Unit
) {
	if (getSingle<Component>() !== null) configureSingle(configure = configure)
	else register(create(), configure = configure)
}


inline fun <Component : RaptorComponent> RaptorComponentRegistry.configureSingleOrCreate(
	clazz: KClass<Component>,
	create: () -> Component
): RaptorConfigurable<Component> =
	if (getSingle(clazz) !== null) configureSingle(clazz = clazz)
	else register(component = create(), clazz = clazz)


inline fun <Component : RaptorComponent> RaptorComponentRegistry.configureSingleOrCreate(
	clazz: KClass<Component>,
	create: () -> Component,
	noinline configure: RaptorConfigurable<Component>.() -> Unit
) {
	if (getSingle(clazz) !== null) configureSingle(clazz = clazz, configure = configure)
	else register(component = create(), clazz = clazz, configure = configure)
}


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.getAll(): List<Component> =
	getAll(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.getSingle(): Component? =
	getSingle(Component::class)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.register(
	component: Component
) =
	register(
		component = component,
		clazz = Component::class
	)


inline fun <reified Component : RaptorComponent> RaptorComponentRegistry.register(
	component: Component,
	noinline configure: RaptorConfigurable<Component>.() -> Unit
) {
	register(
		component = component,
		clazz = Component::class,
		configure = configure
	)
}


fun <Component : RaptorComponent> RaptorComponentRegistry.register(
	component: Component,
	clazz: KClass<Component>,
	configure: RaptorConfigurable<Component>.() -> Unit
) {
	register(
		component = component,
		clazz = clazz
	).invoke(configure)
}
