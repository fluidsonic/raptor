package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.*


internal class RaptorTransactionKtorFeature(
	private val scope: KtorServerScopeImpl
) : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Raptor: transaction feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Setup) {
			val transaction = scope.context.createTransaction()
			call.attributes.put(transactionAttributeKey, transaction)

			try {
				proceed()
			}
			finally {
				call.attributes.remove(transactionAttributeKey)
			}
		}
	}

	companion object {

		private val transactionAttributeKey = AttributeKey<KtorServerTransactionImpl>("Raptor: transaction")
	}
}
