package io.fluidsonic.raptor


internal class RaptorComponentRegistrationImpl<out Component : RaptorComponent> private constructor(
	override val component: Component,
	override val containingRegistry: RaptorComponentRegistryImpl,
	override val registry: RaptorComponentRegistryImpl
) : RaptorComponentRegistration.Mutable<Component>,
	RaptorComponentConfig<Component> {

	override fun invoke(configure: Component.() -> Unit) {
		configure(component)
	}


	class Collection<Component : RaptorComponent>(
		private val registrations: MutableList<RaptorComponentRegistrationImpl<Component>> = mutableListOf()
	) :
		List<RaptorComponentRegistrationImpl<Component>> by registrations,
		RaptorComponentConfig<Component> {

		private val configurations: MutableList<Component.() -> Unit> = mutableListOf()


		fun addComponent(
			component: Component,
			containingRegistry: RaptorComponentRegistryImpl,
			registry: RaptorComponentRegistryImpl
		): RaptorComponentRegistrationImpl<Component> {
			if (registrations.any { it.component === component })
				error("Cannot register component of ${component::class} since it has already been registered: $component")

			val registration = RaptorComponentRegistrationImpl(
				component = component,
				containingRegistry = containingRegistry,
				registry = registry
			)
			registrations += registration

			val configurations = configurations

			with(registration.component) {
				// We iterate over indices because the configurations we call may append more configurations.
				for (index in configurations.indices)
					configurations[index]()
			}

			return registration
		}


		override fun invoke(configure: Component.() -> Unit) {
			configurations += configure

			// We iterate over indices because the configuration we call may append more components.
			for (index in registrations.indices)
				registrations[index].component.apply(configure)
		}
	}
}
