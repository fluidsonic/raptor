package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*


internal object Keys {

	val lifecycleComponent = RaptorComponentKey<RaptorLifecycleComponent>("lifecycle")
	val lifecycleProperty = RaptorPropertyKey<DefaultLifecycle>("lifecycle")
	val serviceComponent = RaptorComponentKey<RaptorServiceComponent<*>>("service")
}
