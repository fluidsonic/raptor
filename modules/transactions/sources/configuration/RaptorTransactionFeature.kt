package io.fluidsonic.raptor


object RaptorTransactionFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(DefaultRaptorTransactionFactory.PropertyKey, componentRegistry.one(RaptorTransactionComponent.Key).toFactory())
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RaptorTransactionComponent.Key, RaptorTransactionComponent())
	}


	override fun toString() = "transaction feature"
}


fun Raptor.createTransaction(): RaptorTransaction =
	context.createTransaction()


inline fun <Result> Raptor.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	context.withNewTransaction(block)


fun RaptorContext.createTransaction(): RaptorTransaction =
	properties[DefaultRaptorTransactionFactory.PropertyKey]?.createTransaction(context = this)
		?: error("You must install ${RaptorTransactionFeature::class.simpleName} for enabling transaction functionality.")


inline fun <Result> RaptorContext.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	with(createTransaction().asScope()) {
		block()
	}


inline fun <Result> RaptorScope.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	context.withNewTransaction(block)
