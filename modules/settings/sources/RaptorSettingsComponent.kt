package io.fluidsonic.raptor


internal class RaptorSettingsComponent(
	val settings: RaptorSettings
) : RaptorComponent.Default<RaptorSettingsComponent>() {

	object Key : RaptorComponentKey<RaptorSettingsComponent> {

		override fun toString() = "settings"
	}
}
