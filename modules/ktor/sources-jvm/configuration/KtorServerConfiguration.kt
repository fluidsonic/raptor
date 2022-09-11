package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.transactions.*
import io.ktor.server.engine.*
import java.io.*
import kotlinx.coroutines.*


internal class KtorServerConfiguration(
	val connectors: Collection<Connector>,
	val customConfigurations: List<RaptorKtorInitializationScope.() -> Unit>,
	val engineEnvironmentFactory: ((configure: ApplicationEngineEnvironmentBuilder.() -> Unit) -> ApplicationEngineEnvironment),
	val engineFactory: (environment: ApplicationEngineEnvironment) -> ApplicationEngine,
	val forceEncryptedConnection: Boolean,
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
}
