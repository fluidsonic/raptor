package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public class RaptorTransactionComponent internal constructor() : RaptorComponent2.Base() {

	private val configurations: MutableList<RaptorTransactionConfigurationScope.() -> Unit> = mutableListOf()


	@RaptorDsl
	public fun onCreate(action: RaptorTransactionConfigurationScope.() -> Unit) {
		configurations += action
	}


	internal fun toFactory() =
		DefaultRaptorTransactionFactory(configurations = configurations.toList())


	override fun toString(): String = "transaction"


	public companion object;


	internal object Key : RaptorComponentKey2<RaptorTransactionComponent> {

		override fun toString() = "transaction"
	}
}


// FIXME (includeNested = false)
@RaptorDsl
public val RaptorTopLevelConfigurationScope.transactions: RaptorTransactionComponent // FIXME rn component to Transactions?
	get() = componentRegistry2.oneOrRegister(RaptorTransactionComponent.Key, ::RaptorTransactionComponent) // FIXME create here?
