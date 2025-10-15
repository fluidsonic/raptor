package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import java.io.*
import java.security.*
import java.util.concurrent.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.*
import org.slf4j.event.*


internal class RaptorKtorServerInternal(
	private val configuration: KtorServerConfiguration,
	parentContext: RaptorContext,
) : RaptorKtorServer {

	private val stateRef = atomic(State.initial)
	private var nextRoutePluginId = 1

	// TODO child context w/ own DI?
	val context = parentContext


	private fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> createEmbeddedServer(
		engine: KtorServerConfiguration.Engine<TEngine, TConfiguration>,
	): EmbeddedServer<TEngine, TConfiguration> {
		val environment = configuration.applicationEnvironmentFactory {
			log = LoggerFactory.getLogger("io.ktor.Application")
		}

		return embeddedServer(
			factory = engine.factory,
			rootConfig = serverConfig(environment) {
				configuration.customConfiguration(this)
			},
			configure = {
				engine.configure(this)
				configureEngine()
			}
		)
	}


	override var embeddedServer: EmbeddedServer<*, *>? = null
		private set


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Ktor server unless it's in 'initial' state." }

		withContext(configuration.startStopDispatcher) {
			startServerBlocking()
		}

		stateRef.value = State.started
	}


	private fun startServerBlocking() {
		val server = createEmbeddedServer(configuration.engine)

		var exception: Throwable? = null

		// Subscribed only after the engine is created because the engine's subscriptions must be processed first.
		val subscription = server.monitor.subscribe(ApplicationStarting) { application ->
			try {
				application.configure()
			}
			catch (e: Throwable) {
				exception = e
			}
		}

		server.start(wait = false)
		subscription.dispose()

		@Suppress("NAME_SHADOWING")
		exception?.let { exception ->
			try {
				server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
			}
			catch (e: Throwable) {
				exception.addSuppressed(e)
			}

			throw exception
		}

		this.embeddedServer = server
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Ktor server unless it's in 'started' state." }

		withContext(configuration.startStopDispatcher) {
			stopServerBlocking()
		}

		stateRef.value = State.stopped
	}


	private fun stopServerBlocking() {
		checkNotNull(embeddedServer).stop(0, 10, TimeUnit.SECONDS) // TODO

		this.embeddedServer = null
	}


	override val tags: Set<Any>
		get() = configuration.tags


	// TODO rework
	private fun Application.configure() {
		attributes.put(Keys.serverKtorAttribute, this@RaptorKtorServerInternal)

		install(CallLogging) {
			level = Level.INFO
		}

		install(Compression)

		install(DefaultHeaders) {
			header(HttpHeaders.Server, "Raptor")
		}

		// TODO Make configurable.
//		install(CORS) {
//			allowNonSimpleContentTypes = true
//
//			anyHost()
//			exposeHeader(HttpHeaders.WWWAuthenticate)
//			header(HttpHeaders.Authorization)
//			method(HttpMethod.Delete)
//			method(HttpMethod.Patch)
//		}

		install(XForwardedHeaders)

		if (configuration.forceEncryptedConnection)
			install(EncryptionEnforcementKtorPlugin)

		install(RaptorTransactionKtorPlugin) {
			serverContext(this@RaptorKtorServerInternal.context)
			transactionFactory(configuration.transactionFactory)
		}

		for (customConfiguration in configuration.customApplicationConfigurations)
			customConfiguration()

		val rootConfig = configuration.rootRouteConfiguration
		if (rootConfig != null)
			routing {
				configure(rootConfig)
			}
	}


	fun ApplicationEngine.Configuration.configureEngine() {
		for (connector in configuration.connectors)
			when (connector) {
				is KtorServerConfiguration.Connector.Http ->
					connector {
						host = connector.host
						port = connector.port
					}

				is KtorServerConfiguration.Connector.Https -> {
					val keyStore = KeyStore.getInstance("JKS").apply {
						FileInputStream(connector.keyStoreFile).use {
							load(it, connector.keyStorePassword.toCharArray())
						}

						requireNotNull(getKey(connector.keyAlias, connector.privateKeyPassword.toCharArray())) {
							"The specified key ${connector.keyAlias} doesn't exist in the key store ${connector.keyStoreFile}"
						}
					}

					sslConnector(
						keyAlias = connector.keyAlias,
						keyStore = keyStore,
						keyStorePassword = { connector.keyStorePassword.toCharArray() },
						privateKeyPassword = { connector.privateKeyPassword.toCharArray() }
					) {
						host = connector.host
						keyStorePath = connector.keyStoreFile
						port = connector.port
					}
				}
			}
	}


	private fun Route.configure(configuration: KtorRouteConfiguration) {
		var wrapper: (Route.(next: Route.() -> Unit) -> Unit) = configuration.wrapper ?: { it() }
		configuration.host?.let { host ->
			wrapper = { host(host, build = it) }
		}

		wrapper {
			route(configuration.path) {
				for (customConfiguration in configuration.customConfigurations)
					customConfiguration()

				// TODO Rework.
				configuration.transactionFactory?.let { transactionFactory ->
					install(transactionRoutePlugin(transactionFactory, configuration.properties, nextRoutePluginId++))
				}

				for (childConfig in configuration.children)
					configure(childConfig)
			}
		}
	}


	private enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}


private fun transactionRoutePlugin(
	transactionFactory: RaptorTransactionFactory,
	properties: RaptorPropertySet,
	uniqueId: Int,
): RouteScopedPlugin<Unit> =
	/*
	   No idea why, but re-using the same plugin and name causes the plugin to only run on child routes, not on the parent route if it
	   is also installed there. Setting a unique name makes sure the parent plugin is executed first, then the child plugin.
	   Ktor bug? Tested in 3.3.1. --marc
	*/
	createRouteScopedPlugin("RaptorRouteTransactionPlugin.$uniqueId") {
		checkNotNull(route).intercept(ApplicationCallPipeline.Setup) {
			val parentTransaction = context.raptorTransaction
			val parentContext = parentTransaction.context

			val transaction = transactionFactory.createTransaction(
				context = RaptorKtorRouteContext(
					parent = parentTransaction.context,
					properties = properties.withFallback(parentContext.properties),
				)
			)

			call.raptorTransaction = transaction

			try {
				coroutineScope {
					proceed()
				}
			}
			finally {
				call.raptorTransaction = parentTransaction
			}
		}
	}
