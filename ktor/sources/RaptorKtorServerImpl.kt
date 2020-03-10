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


internal class RaptorKtorServerImpl(
	private val config: RaptorKtorServerConfig
) : RaptorKtorServerScope, DKodeinAware { // FIXME hmm

	private val ktorEnvironment = commandLineEnvironment(arrayOf())
	private var engine = embeddedServer(Netty, ktorEnvironment)
	private val stateRef = atomic(State.initial)

	override val dkodein = Kodein.direct { import(config.kodeinModule) }


	override fun beginTransaction(): RaptorKtorServerTransactionImpl { // FIXME here?
		val transaction = RaptorKtorServerTransactionImpl(
			parentScope = this
		)

		// FIXME how access transaction config and call all onStart?

		return transaction
	}


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
		val subscription = ktorEnvironment.monitor.subscribe(ApplicationStarting) { application ->
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

		install(RaptorTransactionKtorFeature(server = this@RaptorKtorServerImpl))

		config.ktorApplicationConfig(this)

		val rootConfig = config.routingConfig
		if (rootConfig != null)
			routing {
				configure(rootConfig)
			}
	}


	private fun Route.configure(config: RaptorKtorRouteConfig) {
		route(config.path) {
			// FIXME kodein

			config.ktorConfig(this)

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
