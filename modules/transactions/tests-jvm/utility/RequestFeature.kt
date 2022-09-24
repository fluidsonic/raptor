package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


private val requestComponentKey = RaptorComponentKey<RequestComponent>("request")
private val requestTransactionFactoryPropertyKey = RaptorPropertyKey<RaptorTransactionFactory>("request transaction factory")


object RequestFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(requestTransactionFactoryPropertyKey, componentRegistry.one(requestComponentKey).complete())
	}


	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(requestComponentKey, RequestComponent())
	}


	override fun toString() = "request"
}


fun RaptorContext.createTransaction(request: Request): RaptorTransaction =
	properties[requestTransactionFactoryPropertyKey]?.createTransaction(context = this) {
		propertyRegistry.register(Request.propertyKey, request)
	}
		?: error("You must install RequestFeature for enabling request-scoped transaction functionality.")


fun RaptorTransaction.createTransaction(request: Request): RaptorTransaction =
	context.createTransaction(request)


@RaptorDsl
val RaptorTopLevelConfigurationScope.requests
	get() = componentRegistry.all(requestComponentKey)
