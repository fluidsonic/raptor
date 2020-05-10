package io.fluidsonic.raptor


internal class DefaultRaptorComponentRegistry(
	override val parent: RaptorComponentRegistry? = null
) : RaptorComponentRegistry {

	private var isFinalized = false
	private val setsByKey: MutableMap<RaptorComponentKey<*>, RegistrationSet<*>> = hashMapOf()


	override fun <Component : RaptorComponent> configure(key: RaptorComponentKey<out Component>): RaptorComponentSet<Component> {
		checkMutable { "Cannot confiure a component during finalization." }

		return getOrCreateSet(key)
	}


	private inline fun checkMutable(message: () -> String) {
		if (isFinalized)
			error(message())
	}


	fun finalize() {
		checkMutable { "Cannot finalize a registry that has already been finalized." }

		isFinalized = true
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent> getOrCreateSet(key: RaptorComponentKey<Component>) =
		setsByKey.getOrPut(key) { RegistrationSet<Component>() } as RegistrationSet<Component>


	@Suppress("UNCHECKED_CAST")
	private fun <Component : RaptorComponent> getSet(key: RaptorComponentKey<Component>) =
		setsByKey[key] as RegistrationSet<Component>?


	override fun isEmpty() =
		setsByKey.values.all { it.isEmpty() }


	override fun <Component : RaptorComponent> many(key: RaptorComponentKey<out Component>): List<Component> =
		getSet(key)?.toList().orEmpty()


	override fun <Component : RaptorComponent> oneOrNull(key: RaptorComponentKey<out Component>): Component? =
		getSet(key)
			?.also { set ->
				check(set.size <= 1) {
					"Expected a single component for key '$key' but ${set.size} have been registered:\n\t" +
						set.joinToString("\n\t")
				}
			}
			?.firstOrNull()


	override fun <Component : RaptorComponent> register(key: RaptorComponentKey<in Component>, component: Component) {
		checkMutable { "Cannot register a component during finalization." }

		getOrCreateSet(key).add(component = component)

		component.extensions[RaptorComponentRegistry.ChildRegistryComponentExtensionKey] = DefaultRaptorComponentRegistry(parent = this)
	}


	override fun toString() = buildString {
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


	private inner class RegistrationSet<Component : RaptorComponent>(
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


		override fun configure(action: Component.() -> Unit) {
			checkMutable { "Cannot confiure a component during finalization." }

			configurations += action

			// We iterate over indices because the configuration we invoke may append more components.
			// Components added while iterating will be configured immediately so we don't have to do that here.
			for (index in components.indices)
				components[index].apply(action)
		}


		override fun toString() = buildString {
			components.forEachIndexed { index, component ->
				if (index > 0)
					append("\n")

				append(component.toString().prependIndent("\t").trimStart())

				val componentRegistry = component.componentRegistry.takeUnless { it.isEmpty() }
				val extensions = component.extensions.toString().ifEmpty { null }

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
}
