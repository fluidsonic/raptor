package io.fluidsonic.raptor.ktor

import io.ktor.server.application.*
import io.ktor.server.engine.*


public interface RaptorKtorServer {

	public val engine: ApplicationEngine?
	public val environment: ApplicationEnvironment
	public val tags: Set<Any>
}
