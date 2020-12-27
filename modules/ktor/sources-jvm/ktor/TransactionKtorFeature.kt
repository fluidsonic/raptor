package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.*


// FIXME rework
internal class RaptorTransactionKtorFeature(
	private val serverContext: RaptorContext,
) : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Raptor: transaction feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Setup) {
			val transaction = serverContext.createTransaction()
			call.attributes.put(attributeKey, transaction)

			try {
				proceed()
			}
			finally {
				call.attributes.remove(attributeKey)
			}
		}
	}


	companion object {

		internal val attributeKey = AttributeKey<RaptorTransaction>("Raptor: server transaction")
	}
}


internal val ApplicationCall.raptorTransaction
	get() = attributes.getOrNull(RaptorTransactionKtorFeature.attributeKey)
		?: error("You must install ${RaptorKtorFeature::class.simpleName} for enabling Raptor functionality.")
