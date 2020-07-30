package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.*


// FIXME
internal val ktorServerTransactionAttributeKey = AttributeKey<RaptorTransaction>("Raptor: server transaction")


internal class RaptorTransactionKtorFeature(
	private val serverContext: RaptorContext
) : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Raptor: transaction feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Setup) {
			val transaction = serverContext.createTransaction()
			call.attributes.put(ktorServerTransactionAttributeKey, transaction)

			try {
				proceed()
			}
			finally {
				call.attributes.remove(ktorServerTransactionAttributeKey)
			}
		}
	}
}


// FIXME we may need type alises everywhere for DSL Markers
internal val ApplicationCall.raptorKtorServerTransaction
	get() = attributes[ktorServerTransactionAttributeKey]
