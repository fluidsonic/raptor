package io.fluidsonic.raptor.ktor

import io.ktor.server.testing.*


public val RaptorKtorServer.testEngine: TestApplicationEngine?
	get() = embeddedServer?.engine as? TestApplicationEngine
