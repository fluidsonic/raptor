package io.fluidsonic.raptor


class RaptorTransactionComponent internal constructor() : RaptorComponent.Default<RaptorTransactionComponent>() {

	internal val configurations: MutableList<RaptorTransactionConfigurationScope.() -> Unit> = mutableListOf()


	internal fun toFactory() =
		DefaultRaptorTransactionFactory(configurations = configurations.toList())


	override fun toString() = "transaction"


	companion object;


	internal object Key : RaptorComponentKey<RaptorTransactionComponent> {

		override fun toString() = "transaction"
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorTransactionComponent>.onCreate(action: RaptorTransactionConfigurationScope.() -> Unit) = configure {
	configurations += action
}


// FIXME (includeNested = false)
@RaptorDsl
val RaptorTopLevelConfigurationScope.transactions
	get() = componentRegistry.configure(RaptorTransactionComponent.Key)
