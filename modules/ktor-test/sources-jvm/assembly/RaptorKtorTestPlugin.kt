package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*


public object RaptorKtorTestPlugin : RaptorPlugin {

	@Suppress("INVISIBLE_MEMBER")
	override fun RaptorPluginInstallationScope.install() {
		install(RaptorKtorPlugin)

		ktor.servers.all {
			applicationEnvironmentFactory(::createTestEnvironment)
			engine(TestEngine)
			custom {
				watchPaths = emptyList()
			}
			startStopDispatcher(Dispatchers.Unconfined)
		}
	}
}
