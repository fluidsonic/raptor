package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*
import kotlin.contracts.*


public object RaptorTransactionFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(
			key = RaptorTransactionFactoryPropertyKey,
			value = componentRegistry2.oneOrNull(RaptorTransactionComponent.Key)?.toFactory() ?: RaptorTransactionFactory.empty,
		)
	}


	override fun toString(): String = "transaction"
}


public fun Raptor.createTransaction(): RaptorTransaction =
	context.createTransaction()


// FIXME rn to .transaction {…} and .transaction()
public inline fun <Result> Raptor.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}

	return context.withNewTransaction(block)
}


public fun RaptorContext.createTransaction(): RaptorTransaction =
	properties[RaptorTransactionFactoryPropertyKey]?.createTransaction(context = this)
		?: throw RaptorFeatureNotInstalledException(RaptorTransactionFeature)


public inline fun <Result> RaptorScope.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}

	return with(context.createTransaction().context) {
		block()
	}
}
