package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*


private val configurationExtensionKey = RaptorComponentExtensionKey<RaptorJobsQuartzMongoPluginConfiguration>("quartz mongo configuration")


public object RaptorJobsQuartzMongoPlugin : RaptorPluginWithConfiguration<RaptorJobsQuartzMongoPluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorJobsQuartzMongoPluginConfiguration =
		jobs.extensions[configurationExtensionKey]
			?: error("jobs.quartzMongo { … } configuration required.")


	override fun RaptorPluginInstallationScope.install() {
		require(RaptorDIPlugin)
		require(RaptorJobsPlugin)
		require(RaptorLifecyclePlugin)

		install(QuartzJobScheduler)
	}


	override fun toString(): String = "jobs-quartz-mongo"
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorJobsComponent>.quartzMongo(configure: RaptorJobsQuartzMongoPluginConfiguration.Builder.() -> Unit) {
	// TODO Check that plugin is installed.

	val configuration = RaptorJobsQuartzMongoPluginConfiguration.Builder().apply(configure).build()

	each {
		check(extensions[configurationExtensionKey] == null) { "Cannot configure jobs.quartzMongo() multiple times." }
		extensions[configurationExtensionKey] = configuration
	}
}
