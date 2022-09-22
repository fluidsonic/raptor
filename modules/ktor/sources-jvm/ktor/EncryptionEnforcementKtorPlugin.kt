package io.fluidsonic.raptor.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*


internal val EncryptionEnforcementKtorPlugin = createApplicationPlugin("Raptor: encryption enforcement feature") {
	onCall { call ->
		val scheme = call.request.origin.scheme
		if (scheme != "https" && scheme != "wss")
			call.respondText("The connection protocol is not secure.", status = HttpStatusCode.BadRequest)
	}
}
