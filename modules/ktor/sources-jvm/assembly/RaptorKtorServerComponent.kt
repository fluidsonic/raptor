package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.netty.handler.codec.http.*
import java.io.*
import kotlinx.coroutines.*


public class RaptorKtorServerComponent internal constructor(
	internal val forceEncryptedConnection: Boolean,
) : RaptorComponent2.Base<RaptorKtorServerComponent>(), RaptorTaggableComponent2, RaptorTransactionGeneratingComponent {

	private val connectors: MutableList<KtorServerConfiguration.Connector> = mutableListOf()
	private val customConfigurations: MutableList<Application.() -> Unit> = mutableListOf()
	private var engineEnvironmentFactory: ((configure: ApplicationEngineEnvironmentBuilder.() -> Unit) -> ApplicationEngineEnvironment)? = null
	private var engineFactory: ((environment: ApplicationEngineEnvironment) -> ApplicationEngine)? = null
	private val features: MutableList<RaptorKtorServerFeature> = mutableListOf()
	private var startStopDispatcher: CoroutineDispatcher? = null


	@RaptorDsl
	public fun custom(configuration: RaptorKtorInitializationScope.() -> Unit) {
		customConfigurations += configuration
	}


	@RaptorDsl
	internal fun engineEnvironmentFactory(factory: (configure: ApplicationEngineEnvironmentBuilder.() -> Unit) -> ApplicationEngineEnvironment) {
		check(engineEnvironmentFactory == null) { "Factory already set." }
		engineEnvironmentFactory = factory
	}


	@RaptorDsl
	internal fun engineFactory(factory: (environment: ApplicationEngineEnvironment) -> ApplicationEngine) {
		check(engineFactory == null) { "Factory already set." }
		engineFactory = factory
	}


	@RaptorDsl
	public fun httpConnector(host: String = "0.0.0.0", port: Int = 80) {
		connectors += KtorServerConfiguration.Connector.Http(
			host = host,
			port = port
		)
	}


	@RaptorDsl
	public fun httpsConnector(
		host: String = "0.0.0.0",
		port: Int = 443,
		keyAlias: String,
		keyStoreFile: File,
		keyStorePassword: String,
		privateKeyPassword: String,
	) {
		connectors += KtorServerConfiguration.Connector.Https(
			host = host,
			port = port,
			keyAlias = keyAlias,
			keyStoreFile = keyStoreFile,
			keyStorePassword = keyStorePassword,
			privateKeyPassword = privateKeyPassword
		)
	}


	@RaptorDsl
	public fun install(feature: RaptorKtorServerFeature) {
		if (features.add(feature))
			with(feature) {
				ConfigurationStartScope().onConfigurationStarted()
			}
	}


	@RaptorDsl
	internal fun startStopDispatcher(dispatcher: CoroutineDispatcher) {
		check(startStopDispatcher == null) { "Dispatcher already set." }
		startStopDispatcher = dispatcher
	}


	@RaptorDsl
	public val routes: RaptorKtorRoutesComponent.Root
		get() = componentRegistry2.oneOrRegister(RaptorKtorRoutesComponent.Root.Key) { RaptorKtorRoutesComponent.Root() }


	// FIXME rn
	internal fun RaptorComponentConfigurationEndScope2.toServerConfigurations(): KtorServerConfiguration {
		val rootRouteConfiguration = componentRegistry2.oneOrNull(RaptorKtorRoutesComponent.Key)
			?.componentRegistry2
			?.many(RaptorKtorRouteComponent.Key)
			?.map { routeComponent ->
				with(routeComponent) {
					toRouteConfigurations()
				}
			}
			?.ifEmpty { null }
			?.let {
				KtorRouteConfiguration(
					children = it,
					customConfigurations = emptyList(),
					host = null,
					path = "",
					properties = RaptorPropertySet.empty(),
					transactionFactory = null,
					wrapper = null,
				)
			}

		return KtorServerConfiguration(
			connectors = connectors.toList(),
			customConfigurations = customConfigurations.toList(),
			engineEnvironmentFactory = engineEnvironmentFactory ?: ::applicationEngineEnvironment,
			engineFactory = engineFactory ?: {
				// TODO make configurable
				embeddedServer(Netty, it) {
					httpServerCodec = {
						HttpServerCodec(
							4 * 4096, // for mmpt-k2-server project
							HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE,
							HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE,
						)
					}
				}
			},
			forceEncryptedConnection = forceEncryptedConnection,
			rootRouteConfiguration = rootRouteConfiguration,
			startStopDispatcher = startStopDispatcher ?: Dispatchers.Default,
			tags = tags(this@RaptorKtorServerComponent),
			transactionFactory = transactionFactory(this@RaptorKtorServerComponent),
		)
	}


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		if (features.isEmpty())
			return

		val scope = ConfigurationEndScope(parent = this)

		for (feature in features)
			with(feature) {
				scope.onConfigurationEnded()
			}
	}


	override fun RaptorComponentConfigurationStartScope2.onConfigurationStarted() {
		transactions.di.provide {
			// FIXME improve
			get<RaptorTransactionContext>().ktorCall ?: error("Cannot find Ktor ApplicationCall.")
		}
	}


	internal object Key : RaptorComponentKey2<RaptorKtorServerComponent> {

		override fun toString() = "server"
	}


	private class ConfigurationEndScope(parent: RaptorComponentConfigurationEndScope2) : RaptorKtorServerFeatureConfigurationEndScope {

		private val serverScope = object :
			RaptorKtorServerFeatureConfigurationEndScope.ServerScope,
			RaptorComponentConfigurationEndScope2 by parent {}


		override fun server(configuration: RaptorKtorServerFeatureConfigurationEndScope.ServerScope.() -> Unit) {
			serverScope.configuration()
		}
	}


	private inner class ConfigurationStartScope : RaptorKtorServerFeatureConfigurationStartScope {

		override val server: RaptorKtorServerComponent
			get() = this@RaptorKtorServerComponent
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.custom(configuration: RaptorKtorInitializationScope.() -> Unit) {
	this {
		custom(configuration)
	}
}


@RaptorDsl
internal fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.engineEnvironmentFactory(
	factory: (
		configure: ApplicationEngineEnvironmentBuilder.() -> Unit,
	) -> ApplicationEngineEnvironment,
) {
	this {
		engineEnvironmentFactory(factory)
	}
}


@RaptorDsl
internal fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.engineFactory(factory: (environment: ApplicationEngineEnvironment) -> ApplicationEngine) {
	this {
		engineFactory(factory)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.httpConnector(host: String = "0.0.0.0", port: Int = 80) {
	this {
		httpConnector(host = host, port = port)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.httpsConnector(
	host: String = "0.0.0.0",
	port: Int = 443,
	keyAlias: String,
	keyStoreFile: File,
	keyStorePassword: String,
	privateKeyPassword: String,
) {
	this {
		httpsConnector(
			host = host,
			port = port,
			keyAlias = keyAlias,
			keyStoreFile = keyStoreFile,
			keyStorePassword = keyStorePassword,
			privateKeyPassword = privateKeyPassword
		)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.install(feature: RaptorKtorServerFeature) {
	this {
		install(feature)
	}
}


@RaptorDsl
public val RaptorAssemblyQuery2<RaptorKtorServerComponent>.routes: RaptorAssemblyQuery2<RaptorKtorRoutesComponent.Root>
	get() = map { it.routes }


@RaptorDsl
internal fun RaptorAssemblyQuery2<RaptorKtorServerComponent>.startStopDispatcher(dispatcher: CoroutineDispatcher) {
	this {
		startStopDispatcher(dispatcher)
	}
}
