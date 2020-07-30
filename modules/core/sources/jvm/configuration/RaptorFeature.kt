package io.fluidsonic.raptor


interface RaptorFeature {

	val id: RaptorFeatureId? get() = null

	fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() = Unit
	fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() = Unit


	companion object


	interface Configurable<out ConfigurationScope : Any> : RaptorFeature {

		fun RaptorTopLevelConfigurationScope.configure(action: ConfigurationScope.() -> Unit)
	}
}
