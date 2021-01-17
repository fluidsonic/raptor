package io.fluidsonic.raptor


public class RaptorTransactionComponent internal constructor() : RaptorComponent.Default<RaptorTransactionComponent>() {

	internal val configurations: MutableList<RaptorTransactionConfigurationScope.() -> Unit> = mutableListOf()


	internal fun toFactory() =
		DefaultRaptorTransactionFactory(configurations = configurations.toList())


	override fun toString(): String = "transaction"


	public companion object;


	internal object Key : RaptorComponentKey<RaptorTransactionComponent> {

		override fun toString() = "transaction"
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorTransactionComponent>.onCreate(action: RaptorTransactionConfigurationScope.() -> Unit) {
	configure {
		configurations += action
	}
}


// FIXME (includeNested = false)
@RaptorDsl
public val RaptorTopLevelConfigurationScope.transactions: RaptorComponentSet<RaptorTransactionComponent>
	get() = componentRegistry.configure(RaptorTransactionComponent.Key)
