package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public interface RaptorTransactionGeneratingComponent : RaptorComponent2 {

	public companion object
}


// FIXME includeNested
@RaptorDsl
public val RaptorAssemblyQuery2<RaptorTransactionGeneratingComponent>.transactions: RaptorAssemblyQuery2<RaptorTransactionComponent>
	get() = map { component ->
		component.componentRegistry2.oneOrRegister(RaptorTransactionComponent.Key, ::RaptorTransactionComponent)
	}


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun RaptorConfigurationEndScope.transactionFactory(component: RaptorTransactionGeneratingComponent): RaptorTransactionFactory =
	component.componentRegistry2.oneOrNull(RaptorTransactionComponent.Key)?.toFactory() ?: RaptorTransactionFactory.empty
