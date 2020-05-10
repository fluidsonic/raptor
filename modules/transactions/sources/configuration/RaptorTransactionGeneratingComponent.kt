package io.fluidsonic.raptor


interface RaptorTransactionGeneratingComponent : RaptorComponent {

	companion object
}


// FIXME includeNested
@RaptorDsl
val RaptorComponentSet<RaptorTransactionGeneratingComponent>.transactions: RaptorComponentSet<RaptorTransactionComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.oneOrRegister(RaptorTransactionComponent.Key) { RaptorTransactionComponent() }
		}
	}


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("unused")
fun RaptorFeatureConfigurationEndScope.transactionFactory(component: RaptorTransactionGeneratingComponent): RaptorTransactionFactory =
	component.componentRegistry.oneOrNull(RaptorTransactionComponent.Key)?.toFactory()
		?: DefaultRaptorTransactionFactory(configurations = emptyList())
