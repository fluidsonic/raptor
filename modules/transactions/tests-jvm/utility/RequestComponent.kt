package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


class RequestComponent : RaptorComponent.Base<RequestComponent>(), RaptorTransactionBoundary<RequestComponent> {

	private var transactionFactory: RaptorTransactionFactory? = null


	fun complete() =
		checkNotNull(transactionFactory)


	override fun RaptorComponentConfigurationEndScope<RequestComponent>.onConfigurationEnded() {
		transactionFactory = transactionFactory()
	}
}
