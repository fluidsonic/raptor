package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*


public object RaptorDomainPlugin : RaptorPluginWithConfiguration<RaptorDomainPluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorDomainPluginConfiguration {
		completeComponents()

		return RaptorDomainPluginConfiguration(
			aggregateDefinitions = componentRegistry.one(Keys.domainComponent).completeIn(this),
		)
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.domainComponent, RaptorDomainComponent(topLevelScope = this))

		require(RaptorDIPlugin)
		require(RaptorLifecyclePlugin)
	}


	override fun toString(): String = "domain"
}


// FIXME lazy
@RaptorDsl
public val RaptorPluginScope<in RaptorDomainPlugin>.domain: RaptorDomainComponent
	get() = componentRegistry.oneOrNull(Keys.domainComponent) ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)
