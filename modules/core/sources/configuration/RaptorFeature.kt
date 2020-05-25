package io.fluidsonic.raptor


interface RaptorFeature {

	val id: RaptorFeatureId? get() = null

	fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() = Unit
	fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() = Unit


	companion object


	interface WithRootComponent<out RootComponent : RaptorComponent> : RaptorFeature {

		val RaptorFeatureConfigurationStartScope.rootComponentKey: RaptorComponentKey<out RootComponent>
	}
}
