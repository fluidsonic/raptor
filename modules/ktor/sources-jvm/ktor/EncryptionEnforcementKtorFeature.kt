package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*


internal object EncryptionEnforcementKtorFeature : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Raptor: encryption enforcement feature")


	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Features) {
			val scheme = call.request.origin.scheme
			if (scheme != "https" && scheme != "wss") {
				call.respondText("The connection protocol is not secure.", status = HttpStatusCode.BadRequest)
				finish()
			}
		}
	}
}
