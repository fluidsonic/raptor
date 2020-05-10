package io.fluidsonic.raptor


@RaptorDsl
class RaptorTransactionComponent internal constructor() : RaptorComponent.Base<RaptorTransactionComponent>() {

	internal val onCreateActions: MutableList<RaptorTransactionCreationScope.() -> Unit> = mutableListOf()


	internal fun finalize() =
		DefaultRaptorTransactionFactory(configurations = onCreateActions.toList())


	override fun toString() = "transaction"


	companion object;


	internal object Key : RaptorComponentKey<RaptorTransactionComponent> {

		override fun toString() = "transaction"
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorTransactionComponent>.onCreate(action: RaptorTransactionCreationScope.() -> Unit) = configure {
	onCreateActions += action
}


@RaptorDsl
val RaptorGlobalConfigurationScope.transactions
	get() = componentRegistry.configure(RaptorTransactionComponent.Key)
