package io.fluidsonic.raptor.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*


internal val EncryptionEnforcementKtorPlugin = createApplicationPlugin(
	name = "Raptor: encryption enforcement",
	createConfiguration = ::EncryptionEnforcementKtorPluginConfig,
) {
	val unencryptedHosts: Set<String> = pluginConfig.unencryptedHosts.toHashSet()

	onCall { call ->
		val origin = call.request.origin
		if (origin.serverHost in unencryptedHosts)
			return@onCall

		val scheme = origin.scheme
		if (scheme != "https" && scheme != "wss")
			call.respondText("The connection protocol is not secure.", status = HttpStatusCode.BadRequest)
	}
}


internal class EncryptionEnforcementKtorPluginConfig {

	internal var unencryptedHosts: Set<String> = emptySet()
		private set


	fun unencryptedHosts(hosts: Set<String>) {
		unencryptedHosts = hosts
	}
}
