package io.fluidsonic.raptor

import kotlin.reflect.*


internal class DefaultRaptorComponentRegistry : RaptorComponentRegistry {

	private val setsByType: MutableMap<KClass<out RaptorComponent>, RegistrationSet<*>> = hashMapOf()


	override fun <Component : RaptorComponent> all(type: KClass<Component>): RaptorComponentSet<Component> =
		getOrCreateSet(type)


	override fun createChildRegistry(): RaptorComponentRegistry =
		DefaultRaptorComponentRegistry()


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent> getOrCreateSet(type: KClass<Component>) =
		setsByType.getOrPut(type) { RegistrationSet<Component>() } as RegistrationSet<Component>


	override fun <Component : RaptorComponent> register(component: Component, type: KClass<Component>) {
		getOrCreateSet(type).add(component = component)
	}


	@Suppress("UNCHECKED_CAST")
	fun <Component : RaptorComponent> registeredComponents(type: KClass<Component>) =
		setsByType[type]?.toList() as Collection<Component>


	private class RegistrationSet<Component : RaptorComponent>(
		private val components: MutableList<Component> = mutableListOf()
	) : RaptorComponentSet<Component>, List<Component> by components {

		private val configurations: MutableList<Component.() -> Unit> = mutableListOf()


		fun add(component: Component) {
			if (components.contains(component))
				error(
					"Cannot register component of type '${component::class.qualifiedName}' since an equal one has already been registered.\n" +
						"\tAlready registered: ${components.first { it == component }}\n" +
						"\tTo be registered: $component"
				)

			components += component

			val configurations = configurations

			with(component) {
				// We iterate over indices because the configurations we invoke may append more configurations.
				// Configurations added while iterating will be invoked immediately so we don't have to do that here.
				for (index in configurations.indices)
					configurations[index]()
			}
		}


		override fun forEach(action: Component.() -> Unit) {
			configurations += action

			// We iterate over indices because the configuration we invoke may append more components.
			// Components added while iterating will be configured immediately so we don't have to do that here.
			for (index in components.indices)
				components[index].apply(action)
		}
	}
}
