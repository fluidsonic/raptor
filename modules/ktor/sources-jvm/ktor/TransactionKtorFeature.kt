package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.application.*
import io.ktor.util.*


// FIXME rework
internal class RaptorTransactionKtorFeature(
	private val serverContext: RaptorContext,
	private val transactionFactory: RaptorTransactionFactory,
) : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Raptor: transaction feature")


	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Setup) {
			val transaction = transactionFactory.createTransaction(serverContext.createTransaction().context) {
				propertyRegistry.register(CallPropertyKey, call)
			}
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


	internal object CallPropertyKey : RaptorPropertyKey<ApplicationCall> {

		override fun toString() = "Ktor application call"
	}
}


internal val ApplicationCall.raptorTransaction
	get() = attributes.getOrNull(RaptorTransactionKtorFeature.attributeKey)
		?: error("You must install ${RaptorKtorFeature::class.simpleName} for enabling Raptor functionality.")
