package io.fluidsonic.raptor

import com.mongodb.client.*
import io.fluidsonic.raptor.RaptorJobsQuartzMongoFeature.Configuration
import io.fluidsonic.raptor.di.*
import io.fluidsonic.stdlib.*
import kotlinx.coroutines.*


public object RaptorJobsQuartzMongoFeature : RaptorFeature.Configurable<Configuration.Builder> {

	public override fun RaptorFeatureConfigurationScope.beginConfiguration(action: Configuration.Builder.() -> Unit) {
		install(RaptorJobsFeature)

		val configuration = Configuration.Builder().apply(action).build()

		di.provide<RaptorJobScheduler> {
			QuartzJobScheduler(
				context = get(),
				dispatcher = configuration.dispatcher,
				database = configuration.database(this),
				registry = get(),
			)
		}

		lifecycle {
			onStart { di.get<RaptorJobScheduler>().castOrNull<QuartzJobScheduler>()?.start() }
			onStop { di.get<RaptorJobScheduler>().castOrNull<QuartzJobScheduler>()?.stop() }
		}
	}


	public class Configuration internal constructor(
		internal val database: RaptorDI.() -> MongoDatabase,
		internal val dispatcher: CoroutineDispatcher,
	) {

		public class Builder {

			private var database: (RaptorDI.() -> MongoDatabase)? = null
			private var dispatcher: CoroutineDispatcher? = null


			internal fun build() =
				Configuration(
					database = database ?: error("A database must be provided."),
					dispatcher = dispatcher ?: Dispatchers.Default,
				)


			@RaptorDsl
			public fun database(url: String, name: String) {
				database { MongoClients.create(url).getDatabase(name) }
			}


			@RaptorDsl
			public fun database(provider: RaptorDI.() -> MongoDatabase) {
				check(database == null) { "Cannot provide multiple databases." }

				database = provider
			}


			@RaptorDsl
			public fun dispatcher(dispatcher: CoroutineDispatcher) {
				check(this.dispatcher == null) { "Cannot provide multiple dispatchers." }

				this.dispatcher = dispatcher
			}
		}
	}
}
