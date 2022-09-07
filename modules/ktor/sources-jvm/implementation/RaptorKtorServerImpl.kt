package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.util.*
import java.io.*
import java.security.*
import java.util.concurrent.*
import kotlin.text.toCharArray
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.*
import org.slf4j.event.*


internal class RaptorKtorServerImpl(
	private val configuration: KtorServerConfiguration,
	parentContext: RaptorContext,
) : RaptorKtorServer {

	private val stateRef = atomic(State.initial)

	// FIXME child context w/ own DI?
	val context = parentContext


	override var engine: ApplicationEngine? = null
		private set


	override val environment = configuration.engineEnvironmentFactory {
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


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Ktor server unless it's in 'initial' state." }

		withContext(configuration.startStopDispatcher) {
			startEngineBlocking()
		}

		stateRef.value = State.started
	}


	private fun startEngineBlocking() {
		val engine = configuration.engineFactory(environment)

		var exception: Throwable? = null

		// Subscribed only after the engine is created because the engine's subscriptions must be processed first.
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
			catch (e: Throwable) {
				exception.addSuppressed(e)
			}

			throw exception
		}

		this.engine = engine
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Ktor server unless it's in 'started' state." }

		withContext(configuration.startStopDispatcher) {
			stopEngineBlocking()
		}

		stateRef.value = State.stopped
	}


	private fun stopEngineBlocking() {
		checkNotNull(engine).stop(0, 10, TimeUnit.SECONDS) // FIXME

		this.engine = null
	}


	override val tags: Set<Any>
		get() = configuration.tags


	// FIXME rework
	private fun Application.configure() {
		attributes.put(attributeKey, this@RaptorKtorServerImpl)

		install(CallLogging) {
			level = Level.INFO
		}

		install(Compression)

		install(DefaultHeaders) {
			header(HttpHeaders.Server, "Raptor")
		}

		// FIXME Make configurable.
//		install(CORS) {
//			allowNonSimpleContentTypes = true
//
//			anyHost()
//			exposeHeader(HttpHeaders.WWWAuthenticate)
//			header(HttpHeaders.Authorization)
//			method(HttpMethod.Delete)
//			method(HttpMethod.Patch)
//		}

		install(XForwardedHeaderSupport)
		if (!configuration.insecure)
			install(EncryptionEnforcementKtorFeature)
		install(RaptorTransactionKtorFeature(
			serverContext = this@RaptorKtorServerImpl.context,
			transactionFactory = configuration.transactionFactory,
		))

		for (customConfiguration in configuration.customConfigurations)
			customConfiguration()

		val rootConfig = configuration.rootRouteConfiguration
		if (rootConfig != null)
			routing {
				configure(rootConfig)
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

				configuration.transactionFactory?.let { transactionFactory ->
					intercept(ApplicationCallPipeline.Setup) {
						val parentTransaction = context.attributes.getOrNull(RaptorTransactionKtorFeature.attributeKey)
							?: return@intercept

						val parentContext = parentTransaction.context

						val transaction = transactionFactory.createTransaction(
							context = RaptorKtorRouteContextImpl(
								parent = parentTransaction.context,
								properties = configuration.properties.withFallback(parentContext.properties)
							)
						)

						call.attributes.put(RaptorTransactionKtorFeature.attributeKey, transaction)

						try {
							proceed()
						}
						finally {
							call.attributes.put(RaptorTransactionKtorFeature.attributeKey, parentTransaction)
						}
					}
				}

				for (childConfig in configuration.children)
					configure(childConfig)
			}
		}
	}


	companion object {

		val attributeKey = AttributeKey<RaptorKtorServerImpl>("Raptor: server")
	}


	private enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}


internal val Application.raptorServerImpl
	get() = attributes.getOrNull(RaptorKtorServerImpl.attributeKey)
		?: error("You must install ${RaptorKtorFeature::class.simpleName} for enabling Raptor functionality.")
