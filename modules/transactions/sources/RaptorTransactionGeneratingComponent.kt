package io.fluidsonic.raptor


@RaptorDsl
interface RaptorTransactionGeneratingComponent : RaptorComponent {

	companion object
}


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
fun RaptorFeatureFinalizationScope.transactionFactory(component: RaptorTransactionGeneratingComponent): RaptorTransactionFactory =
	component.componentRegistry.oneOrNull(RaptorTransactionComponent.Key)?.finalize()
		?: DefaultRaptorTransactionFactory(configurations = emptyList())
