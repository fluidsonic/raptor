package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.*


// FIXME
internal val ktorServerTransactionAttributeKey = AttributeKey<KtorServerTransactionInternal>("Raptor: server transaction")


internal class RaptorTransactionKtorFeature(
	private val serverContext: KtorServerContextImpl
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

	companion object {

	}
}


internal val ApplicationCall.raptorKtorServerTransaction
	get() = attributes[ktorServerTransactionAttributeKey]


internal val RaptorKtorPipelineContext.raptorKtorServerTransaction
	get() = context.raptorKtorServerTransaction
