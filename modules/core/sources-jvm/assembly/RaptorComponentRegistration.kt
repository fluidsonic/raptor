package io.fluidsonic.raptor


internal class RaptorComponentRegistration<Component : RaptorComponent<Component>>(
	val component: Component,
	var configurationEnded: Boolean = false,
	var configurationStarted: Boolean = false,
	val registry: DefaultComponentRegistry,
) {

	init {
		component.registration = this
	}


	fun endConfiguration(scope: RaptorConfigurationEndScope) {
		if (configurationEnded)
			return

		configurationEnded = true

		// FIXME Rework.
		val componentScope = object : RaptorComponentConfigurationEndScope<Component>, RaptorConfigurationEndScope by scope {

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
