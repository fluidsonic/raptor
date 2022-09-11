package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


public object RaptorJobsFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(RaptorJobsComponent.Key, RaptorJobsComponent())
	}


	override fun RaptorFeatureConfigurationScope.completeConfiguration() {
		val component = componentRegistry.one(RaptorJobsComponent.Key)
		val registry = component.createRegistry()

		di { provide(registry) }
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.jobs: RaptorComponentSet<RaptorJobsComponent>
	get() = componentRegistry.configure(RaptorJobsComponent.Key)
