package io.fluidsonic.raptor


internal class DefaultComponentRegistry(
	override val parent: RaptorComponentRegistry? = null,
) : RaptorComponentRegistry {

	//	private var configurationEnded = false // TODO Needs a per-plugin check.
	private val setsByKey: MutableMap<RaptorComponentKey<*>, RegistrationSet<*>> = hashMapOf()


	override fun <Component : RaptorComponent<out Component>> all(key: RaptorComponentKey<out Component>): RaptorComponentSet<Component> {
		checkIsConfigurable { "Cannot configure a component after the configuration phase has ended." }

		return getOrCreateSet(key)
	}


	private inline fun checkIsConfigurable(@Suppress("UNUSED_PARAMETER") message: () -> String) {
		// TODO
//		if (configurationEnded)
//			error(message())
	}


	fun complete(plugin: RaptorPluginWithConfiguration<*>, scope: RaptorAssemblyCompletionScope) {
		checkIsConfigurable { "The configuration phase has already ended." }

//		configurationEnded = true

		for (set in setsByKey.values)
			for (registration in set)
				registration.registry.complete(plugin = plugin, scope = scope)

		for (set in setsByKey.values)
			for (registration in set)
				registration.complete(plugin = plugin, scope = scope)
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent<Component>> getOrCreateSet(key: RaptorComponentKey<Component>) =
		setsByKey.getOrPut(key) { RegistrationSet(key = key) } as RegistrationSet<Component>


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent<Component>> getSet(key: RaptorComponentKey<Component>) =
		setsByKey[key] as RegistrationSet<Component>?


	override fun isEmpty() =
		setsByKey.values.all { it.isEmpty() }


	override fun <Component : RaptorComponent<out Component>> many(key: RaptorComponentKey<out Component>): List<Component> =
		getSet(key)?.asCollection()?.map { it.component }.orEmpty()


	override fun <Component : RaptorComponent<out Component>> oneOrNull(key: RaptorComponentKey<out Component>): Component? =
		getSet(key)
			?.also { set ->
				check(set.size <= 1) {
					"Expected a single component for key '$key' but ${set.size} have been registered:\n\t" +
						set.joinToString("\n\t")
				}
			}
			?.firstOrNull()
			?.component


	// TODO Throw when registering a component of a plugin that isn't installed.
	override fun <Component : RaptorComponent<in Component>> register(key: RaptorComponentKey<in Component>, component: Component): Component {
		checkIsConfigurable { "Cannot register a component after the configuration phase has ended." }

		getOrCreateSet(key).add(
			component = component,
			registry = DefaultComponentRegistry(parent = this)
		) {
			with(component) {
				StartScope.onConfigurationStarted()
			}
		}

		return component
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


	private inner class RegistrationSet<Component : RaptorComponent<Component>>(
		private val key: RaptorComponentKey<Component>,
		private val registrations: MutableList<RaptorComponentRegistration<Component>> = mutableListOf(),
	) : RaptorComponentSet<Component>,
		RaptorAssemblyQuery<Component>,
		List<RaptorComponentRegistration<Component>> by registrations {

		private val configurations: MutableList<Component.() -> Unit> = mutableListOf()


		inline fun add(
			component: Component,
			registry: DefaultComponentRegistry,
			beforeConfiguration: (registration: RaptorComponentRegistration<Component>) -> Unit,
		) {
			registrations.firstOrNull { it.component == component }?.let { existingComponent ->
				error(
					"Cannot register component for key '$key' because it has already been registered.\n" +
						"\tAlready registered: $existingComponent\n" +
						"\tTo be registered: $component"
				)
			}

			val registration = RaptorComponentRegistration(
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


		fun asCollection(): Collection<RaptorComponentRegistration<Component>> =
			registrations


		override val all: RaptorAssemblyQuery<Component>
			get() = this


		override fun each(configure: Component.() -> Unit) {
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


	private object StartScope : RaptorComponentConfigurationStartScope
}
