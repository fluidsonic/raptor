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
) : RaptorComponent.Base<RaptorKtorServerComponent>(RaptorKtorPlugin),
	RaptorTaggableComponent<RaptorKtorServerComponent>,
	RaptorTransactionBoundary<RaptorKtorServerComponent> {

	private var configuration: KtorServerConfiguration? = null
	private val connectors: MutableList<KtorServerConfiguration.Connector> = mutableListOf()
	private val customConfigurations: MutableList<Application.() -> Unit> = mutableListOf()
	private var engineEnvironmentFactory: ((configure: ApplicationEngineEnvironmentBuilder.() -> Unit) -> ApplicationEngineEnvironment)? = null
	private var engineFactory: ((environment: ApplicationEngineEnvironment) -> ApplicationEngine)? = null
	private val plugins: MutableList<RaptorKtorServerPlugin> = mutableListOf()
	private var startStopDispatcher: CoroutineDispatcher? = null


	internal fun complete() =
		checkNotNull(configuration)


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
	public fun install(plugin: RaptorKtorServerPlugin) {
		if (plugins.add(plugin))
			with(plugin) {
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
		get() = componentRegistry.oneOrRegister(Keys.rootRoutesComponent) { RaptorKtorRoutesComponent.Root() }


	override fun RaptorComponentConfigurationEndScope<RaptorKtorServerComponent>.onConfigurationEnded() {
		if (plugins.isNotEmpty()) {
			val scope = ConfigurationEndScope(parent = this)

			for (plugin in plugins)
				with(plugin) {
					scope.onConfigurationEnded()
				}
		}

		val rootRouteConfiguration = componentRegistry.oneOrNull(Keys.rootRoutesComponent)?.complete()?.let {
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

		configuration = KtorServerConfiguration(
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
			tags = tags(),
			transactionFactory = transactionFactory(),
		)
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		transactions.di.provide<ApplicationCall> {
			// FIXME improve
			get<RaptorTransactionContext>().ktorCall ?: error("Cannot find Ktor ApplicationCall.")
		}
	}


	private class ConfigurationEndScope(parent: RaptorComponentConfigurationEndScope<RaptorKtorServerComponent>) : RaptorKtorServerPluginConfigurationEndScope {

		private val serverScope = object :
			RaptorKtorServerPluginConfigurationEndScope.ServerScope,
			RaptorComponentConfigurationEndScope<RaptorKtorServerComponent> by parent {}


		override fun server(configuration: RaptorKtorServerPluginConfigurationEndScope.ServerScope.() -> Unit) {
			serverScope.configuration()
		}
	}


	private inner class ConfigurationStartScope : RaptorKtorServerPluginConfigurationStartScope {

		override val server: RaptorKtorServerComponent
			get() = this@RaptorKtorServerComponent
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorServerComponent>.custom(configuration: RaptorKtorInitializationScope.() -> Unit) {
	this {
		custom(configuration)
	}
}


@RaptorDsl
internal fun RaptorAssemblyQuery<RaptorKtorServerComponent>.engineEnvironmentFactory(
	factory: (
		configure: ApplicationEngineEnvironmentBuilder.() -> Unit,
	) -> ApplicationEngineEnvironment,
) {
	this {
		engineEnvironmentFactory(factory)
	}
}


@RaptorDsl
internal fun RaptorAssemblyQuery<RaptorKtorServerComponent>.engineFactory(factory: (environment: ApplicationEngineEnvironment) -> ApplicationEngine) {
	this {
		engineFactory(factory)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorServerComponent>.httpConnector(host: String = "0.0.0.0", port: Int = 80) {
	this {
		httpConnector(host = host, port = port)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorServerComponent>.httpsConnector(
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
public fun RaptorAssemblyQuery<RaptorKtorServerComponent>.install(plugin: RaptorKtorServerPlugin) {
	this {
		install(plugin)
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorKtorServerComponent>.routes: RaptorAssemblyQuery<RaptorKtorRoutesComponent.Root>
	get() = map { it.routes }


@RaptorDsl
internal fun RaptorAssemblyQuery<RaptorKtorServerComponent>.startStopDispatcher(dispatcher: CoroutineDispatcher) {
	this {
		startStopDispatcher(dispatcher)
	}
}
