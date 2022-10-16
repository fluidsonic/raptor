package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


private val rootComponentKey = RaptorComponentKey<RootDIRaptorComponent>("DI")


public object RaptorDIPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(rootComponentKey, RootDIRaptorComponent())
	}


	override fun toString(): String = "DI"
}


@RaptorDsl
public val RaptorPluginScope<in RaptorDIPlugin>.di: RaptorDIComponent<*>
	get() = componentRegistry.oneOrNull(rootComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorDIPlugin)
