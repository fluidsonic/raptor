package tests

import io.fluidsonic.raptor.*


class RequestComponent : RaptorComponent.Base<RequestComponent>(), RaptorTransactionGeneratingComponent {

	object Key : RaptorComponentKey<RequestComponent> {

		override fun toString() = "request"
	}
}


@RaptorDsl
val RaptorGlobalConfigurationScope.requests
	get() = componentRegistry.configure(RequestComponent.Key)
