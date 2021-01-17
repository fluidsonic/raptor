package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.*
import kotlinx.coroutines.*


public class RaptorKtorServerComponent internal constructor(
	internal val globalScope: RaptorTopLevelConfigurationScope,
	internal val insecure: Boolean,
) : RaptorComponent.Default<RaptorKtorServerComponent>(), RaptorTaggableComponent, RaptorTransactionGeneratingComponent {

	// FIXME ok not to specify parent?
	private val propertyRegistry = RaptorPropertyRegistry.default() // FIXME actually use!

	internal var engineEnvironmentFactory: ((configure: ApplicationEngineEnvironmentBuilder.() -> Unit) -> ApplicationEngineEnvironment)? = null
	internal var engineFactory: ((environment: ApplicationEngineEnvironment) -> ApplicationEngine)? = null
	internal val connectors: MutableList<KtorServerConfiguration.Connector> = mutableListOf()
	internal val customConfigurations: MutableList<Application.() -> Unit> = mutableListOf()
	internal val features: MutableList<KtorServerFeature> = mutableListOf()
	internal var startStopDispatcher: CoroutineDispatcher? = null

	internal lateinit var scopes: Scopes


	internal fun RaptorComponentConfigurationEndScope.toServerConfigurations(): KtorServerConfiguration {
		val rootRouteConfiguration = componentRegistry.many(KtorRouteRaptorComponent.Key)
			.map { routeComponent ->
				with(routeComponent) {
					toRouteConfigurations()
				}
			}
			.ifEmpty { null }
			?.let {
				KtorRouteConfiguration(
					children = it,
					customConfigurations = emptyList(),
					path = "",
					properties = RaptorPropertySet.empty(),
					transactionFactory = null,
					wrapper = null
				)
			}

		return KtorServerConfiguration(
			connectors = connectors.toList(),
			customConfigurations = customConfigurations.toList(),
			engineEnvironmentFactory = engineEnvironmentFactory ?: ::applicationEngineEnvironment,
			engineFactory = engineFactory ?: { embeddedServer(Netty, it) },
			insecure = insecure,
			rootRouteConfiguration = rootRouteConfiguration,
			startStopDispatcher = startStopDispatcher ?: Dispatchers.Default,
			tags = tags(this@RaptorKtorServerComponent),
			transactionFactory = transactionFactory(this@RaptorKtorServerComponent),
		)
	}


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		for (feature in features)
			with(feature) {
				scopes.onConfigurationEnded()
			}
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		scopes = Scopes(
			globalScope = globalScope,
			propertyRegistry = propertyRegistry,
			serverComponentRegistry = componentRegistry,
		)

		transactions.di {
			provide {
				// FIXME improve
				get<RaptorTransactionContext>()[RaptorTransactionKtorFeature.CallPropertyKey] ?: error("Cannot find Ktor ApplicationCall.")
			}
		}
	}


	internal object Key : RaptorComponentKey<RaptorKtorServerComponent> {

		override fun toString() = "ktor server"
	}


	internal class Scopes(
		private val globalScope: RaptorTopLevelConfigurationScope,
		propertyRegistry: RaptorPropertyRegistry,
		serverComponentRegistry: RaptorComponentRegistry,
	) : KtorServerFeatureConfigurationEndScope,
		KtorServerFeatureConfigurationStartScope {

		private val serverScope = ServerScope(componentRegistry = serverComponentRegistry, propertyRegistry = propertyRegistry)


		override fun global(configuration: RaptorTopLevelConfigurationScope.() -> Unit) {
			globalScope.configuration()
		}


		override fun server(configuration: KtorServerFeatureConfigurationEndScope.ServerScope.() -> Unit) {
			serverScope.configuration()
		}


		private class ServerScope(
			override val componentRegistry: RaptorComponentRegistry,
			override val propertyRegistry: RaptorPropertyRegistry,
		) : KtorServerFeatureConfigurationEndScope.ServerScope
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.custom(configuration: RaptorKtorInitializationScope.() -> Unit) {
	configure {
		customConfigurations += configuration
	}
}


@RaptorDsl
internal fun RaptorComponentSet<RaptorKtorServerComponent>.engineEnvironmentFactory(
	factory: (configure: ApplicationEngineEnvironmentBuilder.() -> Unit) -> ApplicationEngineEnvironment,
) {
	configure {
		check(engineEnvironmentFactory == null)
		engineEnvironmentFactory = factory
	}
}


@RaptorDsl
internal fun RaptorComponentSet<RaptorKtorServerComponent>.engineFactory(
	factory: (environment: ApplicationEngineEnvironment) -> ApplicationEngine,
) {
	configure {
		check(engineFactory == null)
		engineFactory = factory
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.httpConnector(
	host: String = "0.0.0.0",
	port: Int = 80,
) {
	configure {
		connectors += KtorServerConfiguration.Connector.Http(
			host = host,
			port = port
		)
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.httpsConnector(
	host: String = "0.0.0.0",
	port: Int = 443,
	keyAlias: String,
	keyStoreFile: File,
	keyStorePassword: String,
	privateKeyPassword: String,
) {
	configure {
		connectors += KtorServerConfiguration.Connector.Https(
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
public fun RaptorComponentSet<RaptorKtorServerComponent>.install(feature: KtorServerFeature) {
	configure {
		if (features.add(feature))
			with(feature) {
				scopes.onConfigurationStarted()
			}
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.newRoute(path: String = ""): RaptorComponentSet<KtorRouteRaptorComponent> =
	withComponentAuthoring {
		map {
			KtorRouteRaptorComponent(
				globalScope = globalScope,
				path = path,
				serverComponentRegistry = componentRegistry
			)
				.also { componentRegistry.register(KtorRouteRaptorComponent.Key, it) }
		}
	}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.newRoute(path: String = "", configure: KtorRouteRaptorComponent.() -> Unit) {
	configure {
		KtorRouteRaptorComponent(
			globalScope = globalScope,
			path = path,
			serverComponentRegistry = componentRegistry
		)
			.also { componentRegistry.register(KtorRouteRaptorComponent.Key, it) }
			.also(configure)
	}
}


@RaptorDsl
public val RaptorComponentSet<RaptorKtorServerComponent>.routes: RaptorComponentSet<KtorRouteRaptorComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(KtorRouteRaptorComponent.Key)
		}
	}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.routes(recursive: Boolean): RaptorComponentSet<KtorRouteRaptorComponent> =
	withComponentAuthoring {
		when (recursive) {
			true -> componentSet { action ->
				authoredSet.routes {
					action()
					routes(recursive = true).configure(action)
				}
			}
			false -> authoredSet.routes
		}
	}


@RaptorDsl
public fun RaptorComponentSet<RaptorKtorServerComponent>.routes(recursive: Boolean, configure: KtorRouteRaptorComponent.() -> Unit) {
	routes(recursive = recursive).invoke(configure)
}


@RaptorDsl
internal fun RaptorComponentSet<RaptorKtorServerComponent>.startStopDispatcher(
	dispatcher: CoroutineDispatcher,
) {
	configure {
		check(startStopDispatcher == null)
		startStopDispatcher = dispatcher
	}
}
