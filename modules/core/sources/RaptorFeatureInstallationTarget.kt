package io.fluidsonic.raptor

import kotlin.reflect.*


interface RaptorFeatureInstallationTarget {

	@RaptorDsl
	fun <Feature : RaptorConfigurableFeature<RootComponent>, RootComponent : RaptorComponent> install(
		feature: Feature,
		rootComponentType: KClass<RootComponent>,
		configure: RootComponent.() -> Unit = {}
	)


	companion object
}
