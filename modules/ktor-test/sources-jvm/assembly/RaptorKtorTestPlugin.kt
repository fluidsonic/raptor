package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*


public object RaptorKtorTestPlugin : RaptorPlugin {

	@Suppress("INVISIBLE_MEMBER")
	override fun RaptorPluginInstallationScope.install() {
		install(RaptorKtorPlugin)

		ktor.servers.all {
			applicationEnvironmentFactory(::createTestEnvironment)
			engineFactory { environment ->
				// FIXME ok?
				EmbeddedServer(serverConfig(environment) {
					watchPaths = emptyList()
				}, TestEngine).engine
			}
			startStopDispatcher(Dispatchers.Unconfined)
		}
	}
}
