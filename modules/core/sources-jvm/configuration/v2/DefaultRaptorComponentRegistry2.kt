package io.fluidsonic.raptor


internal class DefaultRaptorComponentRegistry2(
	override val parent: RaptorComponentRegistry2? = null,
) : RaptorComponentRegistry2 {

	private var configurationEnded = false
	private val setsByKey: MutableMap<RaptorComponentKey2<*>, RegistrationSet<*>> = hashMapOf()


	override fun <Component : RaptorComponent2> all(key: RaptorComponentKey2<out Component>): RaptorComponentSet2<Component> {
		checkIsConfigurable { "Cannot configure a component after the configuration phase has ended." }

		return getOrCreateSet(key)
	}


	private inline fun checkIsConfigurable(message: () -> String) {
		if (configurationEnded)
			error(message())
	}


	fun endConfiguration(scope: RaptorComponentConfigurationEndScope2) {
		checkIsConfigurable { "The configuration phase has already ended." }

		configurationEnded = true

		for (set in setsByKey.values)
			for (registration in set)
				registration.registry.endConfiguration(scope = scope)

		for (set in setsByKey.values)
			for (registration in set)
				with(registration.component) {
					scope.onConfigurationEnded()
				}
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent2> getOrCreateSet(key: RaptorComponentKey2<Component>) =
		setsByKey.getOrPut(key) { RegistrationSet(key = key) } as RegistrationSet<Component>


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent2> getSet(key: RaptorComponentKey2<Component>) =
		setsByKey[key] as RegistrationSet<Component>?


	override fun isEmpty() =
		setsByKey.values.all { it.isEmpty() }


	override fun <Component : RaptorComponent2> many(key: RaptorComponentKey2<out Component>): List<Component> =
		getSet(key)?.map { it.component }.orEmpty()


	override fun <Component : RaptorComponent2> oneOrNull(key: RaptorComponentKey2<out Component>): Component? =
		getSet(key)
			?.also { set ->
				check(set.size <= 1) {
					"Expected a single component for key '$key' but ${set.size} have been registered:\n\t" +
						set.joinToString("\n\t")
				}
			}
			?.firstOrNull()
			?.component


	override fun <Component : RaptorComponent2> register(key: RaptorComponentKey2<in Component>, component: Component) {
		checkIsConfigurable { "Cannot register a component after the configuration phase has ended." }

		getOrCreateSet(key).add(
			component = component,
			registry = DefaultRaptorComponentRegistry2(parent = this)
		) { registration ->
			with(component) {
				extensions[RaptorComponentRegistryExtensionKey2] = registration.registry

				StartScope.onConfigurationStarted()
			}
		}
	}


	override fun toString(): String = buildString {
		val entries = setsByKey.entries
			.filter { (_, set) -> set.isNotEmpty() }

		append("[component registry] ->")

		if (entries.isEmpty()) {
			append(" (empty)")
			return@buildString
		}

		append("\n")
		entries
			.map { (key, set) ->
				key.toString() to set.toString()
			}
			.sortedBy { (key) -> key }
			.forEachIndexed { index, (key, set) ->
				if (index > 0)
					append("\n")

				append("[$key]".prependIndent("\t"))
				append(" ->")

				if (set.contains('\n')) {
					append("\n")
					append(set.prependIndent("\t\t"))
				}
				else {
					append(" ")
					append(set)
				}
			}
	}


	private inner class RegistrationSet<Component : RaptorComponent2>(
		private val key: RaptorComponentKey2<Component>,
		private val registrations: MutableList<Registration<Component>> = mutableListOf(),
	) : RaptorComponentSet2<Component>, List<Registration<Component>> by registrations {

		private val configurations: MutableList<Component.() -> Unit> = mutableListOf()


		inline fun add(
			component: Component,
			registry: DefaultRaptorComponentRegistry2,
			beforeConfiguration: (registration: Registration<Component>) -> Unit,
		) {
			registrations.firstOrNull { it.component == component }?.let { existingComponent ->
				error(
					"Cannot register component for key '$key' because it has already been registered.\n" +
						"\tAlready registered: $existingComponent\n" +
						"\tTo be registered: $component"
				)
			}

			val registration = Registration(
				component = component,
				registry = registry
			)
			registrations += registration

			beforeConfiguration(registration)

			registration.configurationStarted = true

			val configurations = configurations

			with(component) {
				// We iterate over indices because the configurations we invoke may append more configurations.
				// Configurations added while iterating will be invoked immediately, so we don't have to do that here.
				for (index in configurations.indices)
					configurations[index]()
			}
		}


		override fun all(configure: Component.() -> Unit) {
			checkIsConfigurable { "Cannot configure a component after the configuration phase has ended." }

			configurations += configure

			// We iterate over indices because the configuration we invoke may append more components.
			// Components added while iterating will be configured immediately, so we don't have to do that here.
			for (index in registrations.indices)
				registrations[index]
					.takeIf { it.configurationStarted }
					?.component
					?.apply(configure)
		}


		override fun toString(): String = buildString {
			registrations.forEachIndexed { index, registration ->
				if (index > 0)
					append("\n")

				append(registration.component.toString().prependIndent("\t").trimStart())

				val componentRegistry = registration.registry.takeUnless { it.isEmpty() }
				val extensions = registration.component.extensions.toString().ifEmpty { null }

				if (extensions != null || componentRegistry != null) {
					append(" -> \n")

					if (extensions != null) {
						append(extensions.prependIndent("\t"))
					}

					if (componentRegistry != null) {
						if (extensions != null)
							append("\n")

						append(componentRegistry.toString().prependIndent("\t"))
					}
				}
			}
		}
	}


	private class Registration<Component : RaptorComponent2>(
		val component: Component,
		var configurationStarted: Boolean = false,
		val registry: DefaultRaptorComponentRegistry2,
	)


	private object StartScope : RaptorComponentConfigurationStartScope2
}
