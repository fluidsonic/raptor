package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


class RequestComponent : RaptorComponent2.Base(), RaptorTransactionGeneratingComponent {

	object Key : RaptorComponentKey2<RequestComponent> {

		override fun toString() = "request"
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.requests
	get() = componentRegistry2.all(RequestComponent.Key)
