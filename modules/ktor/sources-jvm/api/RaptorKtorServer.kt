package io.fluidsonic.raptor.ktor

import io.ktor.server.engine.*


public interface RaptorKtorServer {

	public val embeddedServer: EmbeddedServer<*, *>?
	public val tags: Set<Any>
}
