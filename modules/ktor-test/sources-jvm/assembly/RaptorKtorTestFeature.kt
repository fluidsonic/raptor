package io.fluidsonic.raptor

import io.ktor.server.testing.*
import kotlinx.coroutines.*


public object RaptorKtorTestFeature : RaptorFeature {

	override val id: RaptorFeatureId = "raptor.ktor.test"


	@Suppress("INVISIBLE_MEMBER")
	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		install(RaptorKtorFeature)

		ktor.servers {
			engineEnvironmentFactory(::createTestEnvironment)
			engineFactory(::TestApplicationEngine)
			startStopDispatcher(Dispatchers.Unconfined)
		}
	}
}
