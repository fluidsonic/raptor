package io.fluidsonic.raptor

import org.kodein.di.*


class RaptorTransactionComponent internal constructor() : RaptorComponent {

	override val raptorSetupContext: RaptorSetupContext
		get() = TODO()
}


// FIXME generalize
@Raptor.Dsl3
fun RaptorConfigurable<RaptorTransactionComponent>.kodein(config: Kodein.Builder.() -> Unit) { // FIXME make own API
	forEachComponent {
		TODO()
	}
}
