package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


object RequestFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(RequestTransactionFactoryRaptorPropertyKey, transactionFactory(componentRegistry2.one(RequestComponent.Key)))
	}


	override fun RaptorFeatureScope.installed() {
		componentRegistry2.register(RequestComponent.Key, RequestComponent())
	}


	override fun toString() = "request"
}


fun RaptorContext.createTransaction(request: Request): RaptorTransaction =
	properties[RequestTransactionFactoryRaptorPropertyKey]?.createTransaction(context = this) {
		propertyRegistry.register(RequestRaptorPropertyKey, request)
	}
		?: error("You must install RequestFeature for enabling request-scoped transaction functionality.")


fun RaptorTransaction.createTransaction(request: Request): RaptorTransaction =
	context.createTransaction(request)
