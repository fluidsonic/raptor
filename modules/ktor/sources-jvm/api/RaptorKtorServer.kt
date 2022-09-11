package io.fluidsonic.raptor.ktor

import io.ktor.server.engine.*


public interface RaptorKtorServer {

	public val engine: ApplicationEngine?
	public val environment: ApplicationEngineEnvironment
	public val tags: Set<Any>
}
