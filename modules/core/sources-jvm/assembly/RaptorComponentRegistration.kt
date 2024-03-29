package io.fluidsonic.raptor


internal class RaptorComponentRegistration<Component : RaptorComponent<Component>>(
	val component: Component,
	private var configurationEnded: Boolean = false,
	var configurationStarted: Boolean = false,
	val registry: DefaultComponentRegistry,
) {

	init {
		component.registration = this
	}


	fun complete(plugin: RaptorPluginWithConfiguration<*>, scope: RaptorAssemblyCompletionScope) {
		if (configurationEnded)
			return

		if (component.plugin != plugin)
			return

		configurationEnded = true

		// TODO Rework.
		val componentScope = object : RaptorComponentConfigurationEndScope<Component>, RaptorAssemblyCompletionScope by scope {

			override val component: Component
				get() = this@RaptorComponentRegistration.component


			override val componentRegistry: RaptorComponentRegistry
				get() = registry
		}

		with(component) {
			componentScope.onConfigurationEnded()
		}
	}


	override fun toString() =
		"component registration <$component>"
}


@RaptorDsl
@Suppress("UNCHECKED_CAST")
internal var <Component : RaptorComponent<out Component>> Component.registration: RaptorComponentRegistration<out Component>
	get() = extensions[Keys.registrationComponentExtension] as RaptorComponentRegistration<out Component>? ?: error("Component hasn't been registered.")
	private set(value) {
		extensions[Keys.registrationComponentExtension] = value
	}
