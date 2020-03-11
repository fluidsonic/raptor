package io.fluidsonic.raptor

import org.kodein.di.*


class RaptorTransactionComponent internal constructor() : RaptorComponent


// FIXME generalize
@Raptor.Dsl3
fun RaptorConfigurable<RaptorTransactionComponent>.kodein(configure: Kodein.Builder.() -> Unit) { // FIXME make own API
	raptorComponentConfiguration {
		TODO()
	}
}
