package tests

import io.fluidsonic.raptor.*


object RequestFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		propertyRegistry.register(RequestTransactionFactoryRaptorPropertyKey, transactionFactory(componentRegistry.one(RequestComponent.Key)))
	}


	override fun RaptorFeatureInstallationScope.install() {
		componentRegistry.register(RequestComponent.Key, RequestComponent())
	}


	override fun toString() = "request"
}


fun RaptorContext.createTransaction(request: Request): RaptorTransaction =
	properties[RequestTransactionFactoryRaptorPropertyKey]?.createTransaction(context = this) {
		propertyRegistry.register(RequestRaptorPropertyKey, request)
	}
		?: error("You must install RequestFeature to create request-scoped transactions.")


fun RaptorTransaction.createTransaction(request: Request): RaptorTransaction =
	context.createTransaction(request)
