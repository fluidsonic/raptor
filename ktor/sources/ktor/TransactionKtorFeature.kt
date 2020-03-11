package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.*


private val transactionKtorAttributeKey = AttributeKey<KtorServerTransactionImpl>("Raptor: transaction")


internal class RaptorTransactionKtorFeature(
	private val server: KtorServer
) : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Raptor: transaction feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Setup) {
			val transaction = server.beginTransaction()
			var exception: Throwable? = null

			try {
				call.attributes.put(transactionKtorAttributeKey, transaction)
			}
			catch (e: Throwable) {
				exception = e

				throw e
			}
			finally {
				try {
					transaction.complete()
				}
				catch (e: Throwable) {
					if (exception != null) exception.addSuppressed(e)
					else throw e
				}
				finally {
					call.attributes.remove(transactionKtorAttributeKey)
				}
			}
		}
	}
}
