package io.fluidsonic.raptor


class RaptorConfigurationComponent : RaptorComponent.Default<RaptorConfigurationComponent>() {

	internal val configurations: MutableList<RaptorConfiguration> = mutableListOf()


	override fun toString() = "configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(ConfigurationRaptorPropertyKey, when (configurations.size) {
			0 -> RaptorConfiguration.empty
			1 -> configurations.single()
			else -> RaptorConfiguration.lookup(configurations.toList().reversed())
		})
	}


	companion object;


	internal object Key : RaptorComponentKey<RaptorConfigurationComponent> {

		override fun toString() = "configuration"
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorConfigurationComponent>.append(configuration: RaptorConfiguration) {
	configure {
		configurations += configuration
	}
}
