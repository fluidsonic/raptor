package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


private val jobComponentKey = RaptorComponentKey<RaptorJobsComponent>("jobs")


public object RaptorJobsPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		val component = componentRegistry.one(jobComponentKey)
		val registry = component.complete()

		configure(RaptorDIPlugin) {
			di.provide<RaptorJobRegistry>(registry)
		}
	}


	override fun RaptorPluginInstallationScope.install() {
		require(RaptorDIPlugin)

		componentRegistry.register(jobComponentKey, RaptorJobsComponent())
	}


	override fun toString(): String = "jobs"
}


@RaptorDsl
public val RaptorAssemblyScope.jobs: RaptorJobsComponent
	get() = componentRegistry.oneOrNull(jobComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorJobsPlugin)
