package io.fluidsonic.raptor

import com.mongodb.client.*
import io.fluidsonic.raptor.di.*
import kotlinx.coroutines.*


public class RaptorJobsQuartzMongoPluginConfiguration internal constructor(
	internal val database: RaptorDI.() -> MongoDatabase,
	internal val dispatcher: CoroutineDispatcher,
) {

	public class Builder {

		private var database: (RaptorDI.() -> MongoDatabase)? = null
		private var dispatcher: CoroutineDispatcher? = null


		internal fun build() =
			RaptorJobsQuartzMongoPluginConfiguration(
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

