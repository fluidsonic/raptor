package tests

import io.fluidsonic.raptor.*


class RequestComponent : RaptorComponent.Default<RequestComponent>(), RaptorTransactionGeneratingComponent {

	object Key : RaptorComponentKey<RequestComponent> {

		override fun toString() = "request"
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.requests
	get() = componentRegistry.configure(RequestComponent.Key)
