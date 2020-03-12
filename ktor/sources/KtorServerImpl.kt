package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.kodein.di.*
import org.slf4j.event.*
import java.util.concurrent.*


internal class KtorServerImpl(
	private val config: KtorServerConfig,
	parentContext: RaptorContext
) {

	private var engine = embeddedServer(Netty, commandLineEnvironment(arrayOf()))
	private val stateRef = atomic(State.initial)

	val context = KtorServerContextImpl(
		dkodein = Kodein.direct { import(config.kodeinModule) },
		parentContext = parentContext
	)


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start RaptorServer unless it's in 'initial' state." }

		try {
			withContext(Dispatchers.Default) {
				startEngineBlocking()
			}
		}
		catch (e: Throwable) {
			stateRef.value = State.stopped

			throw e
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

		engine.start(wait = true)
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
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Raptor unless it's in 'started' state." }

		try {
			withContext(Dispatchers.Default) {
				stopEngineBlocking()
			}
		}
		finally {
			stateRef.value = State.stopped
		}
	}


	private fun stopEngineBlocking() {
		engine.stop(0, 10, TimeUnit.SECONDS) // FIXME
	}


	// FIXME rework
	private fun Application.configure() {
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
		install(RaptorTransactionKtorFeature(scope = context.createScope()))

		config.customConfig(this)

		val rootConfig = config.routingConfig
		if (rootConfig != null)
			routing {
				configure(rootConfig)
			}
	}


	private fun Route.configure(config: KtorRouteConfig) {
		route(config.path) {
			// FIXME kodein

			config.customConfig(this)

			for (childConfig in config.children) {
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
