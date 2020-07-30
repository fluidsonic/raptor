package io.fluidsonic.raptor

import java.io.*


internal class KtorServerConfiguration(
	val connectors: List<Connector>,
	val customConfigurations: List<RaptorKtorConfigurationScope.() -> Unit>,
	val rootRouteConfiguration: KtorRouteConfiguration?,
	val transactionFactory: RaptorTransactionFactory // FIXME use
) {

	sealed class Connector(
		val host: String,
		val port: Int
	) {

		class Http(
			host: String,
			port: Int
		) : Connector(host = host, port = port)


		class Https(
			host: String,
			port: Int,
			val keyAlias: String,
			val keyStoreFile: File,
			val keyStorePassword: String,
			val privateKeyPassword: String
		) : Connector(host = host, port = port)
	}
}
