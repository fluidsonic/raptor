package io.fluidsonic.raptor


@RaptorDsl
interface RaptorTransactionGeneratingComponent : RaptorComponent {// FIXME make all containers?!

	companion object
}


@RaptorDsl
val RaptorComponentSet<RaptorTransactionGeneratingComponent>.transactions: RaptorComponentSet<RaptorTransactionComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.oneOrRegister(RaptorTransactionComponent.Key) { RaptorTransactionComponent() }
		}
	}


@RaptorDsl
@Suppress("unused")
fun RaptorFeatureFinalizationScope.transactionFactory(component: RaptorTransactionGeneratingComponent): RaptorTransactionFactory =
	component.componentRegistry.oneOrNull(RaptorTransactionComponent.Key)?.finalize()
		?: DefaultRaptorTransactionFactory(configurations = emptyList())
