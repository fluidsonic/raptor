package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.event.*
import java.util.concurrent.*


private val ktorServerAttributeKey = AttributeKey<KtorServerImpl>("Raptor: server")


internal class KtorServerImpl(
	private val config: KtorServerConfig,
	parentContext: RaptorContext
) {

	private var engine = embeddedServer(Netty, commandLineEnvironment(arrayOf()))
	private val stateRef = atomic(State.initial)

	val context = KtorServerContextImpl(
		kodeinModule = config.kodeinModule,
		parentContext = parentContext
	)


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Ktor server unless it's in 'initial' state." }

		withContext(Dispatchers.Default) {
			startEngineBlocking()
		}

		stateRef.value = State.started
	}


	private fun startEngineBlocking() {
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
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Ktor server unless it's in 'started' state." }

		withContext(Dispatchers.Default) {
			stopEngineBlocking()
		}

		stateRef.value = State.stopped
	}


	private fun stopEngineBlocking() {
		engine.stop(0, 10, TimeUnit.SECONDS) // FIXME
	}


	// FIXME rework
	private fun Application.configure() {
		attributes.put(ktorServerAttributeKey, this@KtorServerImpl)

		install(CallLogging) {
			level = Level.INFO
		}

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
		install(RaptorTransactionKtorFeature(serverContext = this@KtorServerImpl.context))

		config.customConfig(this)

		val rootConfig = config.routingConfig
		if (rootConfig != null)
			routing {
				configure(rootConfig)
			}
	}


	private fun Route.configure(config: KtorRouteConfig) {
		val wrapper: (Route.(next: Route.() -> Unit) -> Unit) = config.wrapper ?: { it() }
		wrapper {
			route(config.path) {
				config.customConfig(this)

				intercept(ApplicationCallPipeline.Setup) {
					val parentTransaction = context.attributes.getOrNull(ktorServerTransactionAttributeKey)
						?: return@intercept

					val transaction = parentTransaction.createRouteTransaction(config = config)
					call.attributes.put(ktorServerTransactionAttributeKey, transaction)

					try {
						proceed()
					}
					finally {
						call.attributes.put(ktorServerTransactionAttributeKey, parentTransaction)
					}
				}

				for (childConfig in config.children)
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


internal val RaptorKtorApplication.raptorKtorServer
	get() = attributes[ktorServerAttributeKey]
