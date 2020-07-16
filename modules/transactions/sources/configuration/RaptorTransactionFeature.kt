package io.fluidsonic.raptor

import kotlin.contracts.*


object RaptorTransactionFeature : RaptorFeature {

	override val id = raptorTransactionFeatureId


	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(DefaultRaptorTransactionFactory.PropertyKey, componentRegistry.one(RaptorTransactionComponent.Key).toFactory())
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RaptorTransactionComponent.Key, RaptorTransactionComponent())
	}


	override fun toString() = "transaction feature"
}


const val raptorTransactionFeatureId: RaptorFeatureId = "raptor.transaction"


fun Raptor.createTransaction(): RaptorTransaction =
	context.createTransaction()


inline fun <Result> Raptor.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	context.withNewTransaction(block)


fun RaptorContext.createTransaction(): RaptorTransaction =
	properties[DefaultRaptorTransactionFactory.PropertyKey]?.createTransaction(context = this)
		?: error("You must install ${RaptorTransactionFeature::class.simpleName} for enabling transaction functionality.")


inline fun <Result> RaptorScope.withNewTransaction(action: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(action, InvocationKind.EXACTLY_ONCE)
	}

	return with(context.createTransaction().context) {
		action()
	}
}
