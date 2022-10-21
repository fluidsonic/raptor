package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.stdlib.*


private val configurationExtensionKey = RaptorComponentExtensionKey<RaptorJobsQuartzMongoConfiguration>("quartz mongo configuration")


public object RaptorJobsQuartzMongoPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		val configuration = jobs.extensions[configurationExtensionKey] ?: return

		configure(RaptorDIPlugin) {
			di.provide<RaptorJobScheduler> {
				QuartzJobScheduler(
					context = get(),
					dispatcher = configuration.dispatcher,
					database = configuration.database(this),
					registry = get(),
				)
			}
		}

		configure(RaptorLifecyclePlugin) {
			lifecycle {
				onStart(-1) { di.get<RaptorJobScheduler>().castOrNull<QuartzJobScheduler>()?.start() }
				onStop(1) { di.get<RaptorJobScheduler>().castOrNull<QuartzJobScheduler>()?.stop() }
			}
		}
	}


	override fun RaptorPluginInstallationScope.install() {
		install(RaptorJobsPlugin)

		require(RaptorDIPlugin)
		require(RaptorLifecyclePlugin)
	}


	override fun toString(): String = "jobs-quartz-mongo"
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorJobsComponent>.quartzMongo(configure: RaptorJobsQuartzMongoConfiguration.Builder.() -> Unit) {
	// TODO Check that plugin is installed.

	val configuration = RaptorJobsQuartzMongoConfiguration.Builder().apply(configure).build()

	each {
		check(extensions[configurationExtensionKey] == null) { "Cannot configure jobs.quartzMongo() multiple times." }
		extensions[configurationExtensionKey] = configuration
	}
}
