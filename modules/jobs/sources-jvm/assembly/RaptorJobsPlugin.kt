package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


private val jobComponentKey = RaptorComponentKey<RaptorJobsComponent>("jobs")


public object RaptorJobsPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		val component = componentRegistry.one(jobComponentKey)
		val registry = component.complete()

		di { provide(registry) }
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(jobComponentKey, RaptorJobsComponent())
	}
}


@RaptorDsl
public val RaptorAssemblyScope.jobs: RaptorJobsComponent
	get() = componentRegistry.oneOrNull(jobComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorJobsPlugin)
