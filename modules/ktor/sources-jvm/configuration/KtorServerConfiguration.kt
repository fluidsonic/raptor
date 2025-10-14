package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.transactions.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import java.io.*
import kotlinx.coroutines.*


internal class KtorServerConfiguration(
	val applicationEnvironmentFactory: ((configure: ApplicationEnvironmentBuilder.() -> Unit) -> ApplicationEnvironment),
	val connectors: Collection<Connector>,
	val engine: Engine<*, *>,
	val forceEncryptedConnection: Boolean,
	val customApplicationConfigurations: List<RaptorKtorInitializationScope.() -> Unit>,
	val customConfiguration: ServerConfigBuilder.() -> Unit,
	val rootRouteConfiguration: KtorRouteConfiguration?,
	val startStopDispatcher: CoroutineDispatcher,
	val tags: Set<Any>,
	val transactionFactory: RaptorTransactionFactory,
) {

	sealed class Connector(
		val host: String,
		val port: Int,
	) {

		class Http(
			host: String,
			port: Int,
		) : Connector(host = host, port = port)


		class Https(
			host: String,
			port: Int,
			val keyAlias: String,
			val keyStoreFile: File,
			val keyStorePassword: String,
			val privateKeyPassword: String,
		) : Connector(host = host, port = port)
	}


	data class Engine<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
		val configure: TConfiguration.() -> Unit,
		val factory: ApplicationEngineFactory<TEngine, TConfiguration>,
	)
}
