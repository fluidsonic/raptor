package io.fluidsonic.raptor

import kotlin.contracts.*


public object RaptorTransactionFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorTransactionFeatureId


	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(
			key = DefaultRaptorTransactionFactory.PropertyKey,
			value = componentRegistry.one(RaptorTransactionComponent.Key).toFactory()
		)
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(RaptorTransactionComponent.Key, RaptorTransactionComponent())
	}


	override fun toString(): String = "transaction feature"
}


public const val raptorTransactionFeatureId: RaptorFeatureId =
	"raptor.transaction" // FIXME we could use the DSL with `inline` extension, e.g. `raptor.features.transaction`


public fun Raptor.createTransaction(): RaptorTransaction =
	context.createTransaction()


public inline fun <Result> Raptor.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	context.withNewTransaction(block)


public fun RaptorContext.createTransaction(): RaptorTransaction =
	properties[DefaultRaptorTransactionFactory.PropertyKey]?.createTransaction(context = this)
		?: error("You must install ${RaptorTransactionFeature::class.simpleName} for enabling transaction functionality.")


public inline fun <Result> RaptorScope.withNewTransaction(action: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(action, InvocationKind.EXACTLY_ONCE)
	}

	return with(context.createTransaction().context) {
		action()
	}
}
