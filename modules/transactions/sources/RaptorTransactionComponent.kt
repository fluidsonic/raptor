package io.fluidsonic.raptor


@RaptorDsl
class RaptorTransactionComponent internal constructor() : RaptorComponent.Base<RaptorTransactionComponent>() {

	override fun toString() = "transaction"


	companion object;


	internal object Key : RaptorComponentKey<RaptorTransactionComponent> {

		override fun toString() = "transaction"
	}
}


@RaptorDsl
val RaptorGlobalConfigurationScope.transactions
	get() = componentRegistry.oneOrRegister(RaptorTransactionComponent.Key, ::RaptorTransactionComponent)
