package io.fluidsonic.raptor


public interface RaptorTransactionGeneratingComponent : RaptorComponent {

	public companion object
}


// FIXME includeNested
@RaptorDsl
public val RaptorComponentSet<RaptorTransactionGeneratingComponent>.transactions: RaptorComponentSet<RaptorTransactionComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.oneOrRegister(RaptorTransactionComponent.Key) { RaptorTransactionComponent() }
		}
	}


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("unused")
public fun RaptorConfigurationEndScope.transactionFactory(component: RaptorTransactionGeneratingComponent): RaptorTransactionFactory =
	component.componentRegistry.oneOrNull(RaptorTransactionComponent.Key)?.toFactory()
		?: DefaultRaptorTransactionFactory(configurations = emptyList())
