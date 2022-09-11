package io.fluidsonic.raptor


internal class RaptorComponentRegistration<Component : RaptorComponent2>(
	val component: Component,
	var configurationEnded: Boolean = false,
	var configurationStarted: Boolean = false,
	val registry: DefaultRaptorComponentRegistry2,
) {

	init {
		component.extensions[RaptorComponentRegistrationExtensionKey] = this
	}


	fun endConfiguration(scope: RaptorConfigurationEndScope) {
		if (configurationEnded)
			return

		configurationEnded = true

		// FIXME Rework.
		val componentScope = object : RaptorComponentConfigurationEndScope2, RaptorConfigurationEndScope by scope {

			override val componentRegistry2: RaptorComponentRegistry2
				get() = registry
		}

		with(component) {
			componentScope.onConfigurationEnded()
		}
	}
}


private object RaptorComponentRegistrationExtensionKey : RaptorComponentExtensionKey<RaptorComponentRegistration<*>> {

	override fun toString(): String = "registration"
}


@RaptorDsl
@Suppress("UNCHECKED_CAST")
internal val <Component : RaptorComponent2> Component.registration: RaptorComponentRegistration<Component>
	get() = extensions[RaptorComponentRegistrationExtensionKey]
		as RaptorComponentRegistration<Component>?
		?: error("Component hasn't been registered.")
