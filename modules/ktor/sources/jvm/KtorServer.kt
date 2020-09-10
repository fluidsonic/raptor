package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import java.io.*
import java.security.*
import java.util.concurrent.*
import kotlin.text.toCharArray
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.*
import org.slf4j.event.*


private val ktorServerAttributeKey = AttributeKey<KtorServer>("Raptor: server")


internal class KtorServer(
	private val configuration: KtorServerConfiguration,
	parentContext: RaptorContext,
) {

	private var engine: ApplicationEngine? = null
	private val environment = applicationEngineEnvironment {
		log = LoggerFactory.getLogger("io.ktor.Application")

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

						requireNotNull(getKey(connector.keyAlias, connector.privateKeyPassword.toCharArray()) == null) {
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
	private val stateRef = atomic(State.initial)

	// FIXME Support child context w/o Transaction, e.g. for Kodein.
	val context = parentContext


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Ktor server unless it's in 'initial' state." }

		withContext(Dispatchers.Default) {
			startEngineBlocking()
		}

		stateRef.value = State.started
	}


	private fun startEngineBlocking() {
		val engine = embeddedServer(Netty, environment)

		var exception: Throwable? = null

		// Engine is created before monitoring the start event because Netty's subscriptions must be processed first.
		val subscription = engine.environment.monitor.subscribe(ApplicationStarting) { application ->
			try {
				application.configure()
			}
			catch (e: Throwable) {
				exception = e
			}
		}

		engine.start(wait = false)
		subscription.dispose()

		@Suppress("NAME_SHADOWING")
		exception?.let { exception ->
			try {
				engine.stop(gracePeriodMillis = 0, timeoutMillis = 0)
			}
			catch (e: Exception) {
				exception.addSuppressed(e)
			}

			throw exception
		}

		this.engine = engine
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Ktor server unless it's in 'started' state." }

		withContext(Dispatchers.Default) {
			stopEngineBlocking()
		}

		stateRef.value = State.stopped
	}


	private fun stopEngineBlocking() {
		checkNotNull(engine).stop(0, 10, TimeUnit.SECONDS) // FIXME

		this.engine = null
	}


	// FIXME rework
	private fun Application.configure() {
		attributes.put(ktorServerAttributeKey, this@KtorServer)

		install(CallLogging) {
			level = Level.INFO
		}

		install(Compression)

		install(DefaultHeaders) {
			header(HttpHeaders.Server, "Raptor")
		}

		install(CORS) {
			allowNonSimpleContentTypes = true

			anyHost()
			exposeHeader(HttpHeaders.WWWAuthenticate)
			header(HttpHeaders.Authorization)
			method(HttpMethod.Delete)
			method(HttpMethod.Patch)
		}

		install(XForwardedHeaderSupport)
		install(EncryptionEnforcementKtorFeature)
		install(RaptorTransactionKtorFeature(serverContext = this@KtorServer.context))

		for (customConfiguration in configuration.customConfigurations)
			customConfiguration()

		val rootConfig = configuration.rootRouteConfiguration
		if (rootConfig != null)
			routing {
				configure(rootConfig)
			}
	}


	private fun Route.configure(configuration: KtorRouteConfiguration) {
		val wrapper: (Route.(next: Route.() -> Unit) -> Unit) = configuration.wrapper ?: { it() }
		wrapper {
			route(configuration.path) {
				for (customConfiguration in configuration.customConfigurations)
					customConfiguration()

				configuration.transactionFactory?.let { transactionFactory ->
					intercept(ApplicationCallPipeline.Setup) {
						val parentTransaction = context.attributes.getOrNull(ktorServerTransactionAttributeKey)
							?: return@intercept

						val parentContext = parentTransaction.context

						val transaction = transactionFactory.createTransaction(
							context = RaptorKtorRouteContext(
								parent = parentTransaction.context,
								properties = configuration.properties.withFallback(parentContext.properties)
							)
						)

						call.attributes.put(ktorServerTransactionAttributeKey, transaction)

						try {
							proceed()
						}
						finally {
							call.attributes.put(ktorServerTransactionAttributeKey, parentTransaction)
						}
					}
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


internal val Application.raptorKtorServer
	get() = attributes[ktorServerAttributeKey]


@RaptorDsl
val Route.raptorContext: RaptorContext
	get() = application.raptorKtorServer.context
