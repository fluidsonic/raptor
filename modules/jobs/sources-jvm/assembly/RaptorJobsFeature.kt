package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


private val jobComponentKey = RaptorComponentKey<RaptorJobsComponent>("jobs")


public object RaptorJobsFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
		val component = componentRegistry.one(jobComponentKey)
		val registry = component.complete()

		di { provide(registry) }
	}


	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(jobComponentKey, RaptorJobsComponent())
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.jobs: RaptorJobsComponent
	get() = componentRegistry.oneOrNull(jobComponentKey) ?: throw RaptorFeatureNotInstalledException(RaptorJobsFeature)
