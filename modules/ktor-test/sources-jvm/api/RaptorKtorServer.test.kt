package io.fluidsonic.raptor

import io.ktor.server.testing.*


public val RaptorKtorServer.testEngine: TestApplicationEngine?
	get() = engine as? TestApplicationEngine
