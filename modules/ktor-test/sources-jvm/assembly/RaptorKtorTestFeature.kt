package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*


public object RaptorKtorTestFeature : RaptorFeature {

	@Suppress("INVISIBLE_MEMBER")
	override fun RaptorFeatureScope.installed() {
		install(RaptorKtorFeature)

		ktor.servers.all {
			engineEnvironmentFactory(::createTestEnvironment)
			engineFactory(::TestApplicationEngine)
			startStopDispatcher(Dispatchers.Unconfined)
		}
	}
}
