package io.fluidsonic.raptor

import io.ktor.application.*
import java.io.*


// FIXME taggable
class KtorServerRaptorComponent internal constructor(
	internal val globalScope: RaptorTopLevelConfigurationScope,
	internal val insecure: Boolean,
) : RaptorComponent.Default<KtorServerRaptorComponent>(), RaptorTransactionGeneratingComponent {

	// FIXME ok not to specify parent?
	private val propertyRegistry = RaptorPropertyRegistry.default() // FIXME actually use!

	internal val connectors: MutableList<KtorServerConfiguration.Connector> = mutableListOf()
	internal val customConfigurations: MutableList<Application.() -> Unit> = mutableListOf()
	internal val features: MutableList<KtorServerFeature> = mutableListOf()

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
			insecure = insecure,
			rootRouteConfiguration = rootRouteConfiguration,
			transactionFactory = transactionFactory(this@KtorServerRaptorComponent)
		)
	}


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		for (feature in features)
			with(feature) {
				scopes.onConfigurationEnded()
			}
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		Scopes(
			globalScope = globalScope,
			propertyRegistry = propertyRegistry,
			serverComponentRegistry = componentRegistry
		)
	}


	internal object Key : RaptorComponentKey<KtorServerRaptorComponent> {

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
fun RaptorComponentSet<KtorServerRaptorComponent>.custom(configuration: RaptorKtorConfigurationScope.() -> Unit) {
	configure {
		customConfigurations += configuration
	}
}


@RaptorDsl
fun RaptorComponentSet<KtorServerRaptorComponent>.httpConnector(
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
fun RaptorComponentSet<KtorServerRaptorComponent>.httpsConnector(
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
fun RaptorComponentSet<KtorServerRaptorComponent>.install(feature: KtorServerFeature) {
	configure {
		if (features.add(feature))
			with(feature) {
				scopes.onConfigurationStarted()
			}
	}
}


@RaptorDsl
fun RaptorComponentSet<KtorServerRaptorComponent>.newRoute(path: String = ""): RaptorComponentSet<KtorRouteRaptorComponent> =
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
fun RaptorComponentSet<KtorServerRaptorComponent>.newRoute(path: String = "", configure: KtorRouteRaptorComponent.() -> Unit) {
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
val RaptorComponentSet<KtorServerRaptorComponent>.routes: RaptorComponentSet<KtorRouteRaptorComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(KtorRouteRaptorComponent.Key)
		}
	}


@RaptorDsl
fun RaptorComponentSet<KtorServerRaptorComponent>.routes(recursive: Boolean): RaptorComponentSet<KtorRouteRaptorComponent> =
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
fun RaptorComponentSet<KtorServerRaptorComponent>.routes(recursive: Boolean, configure: KtorRouteRaptorComponent.() -> Unit) =
	routes(recursive = recursive).invoke(configure)
