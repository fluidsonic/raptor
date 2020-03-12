package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
interface RaptorTransactionSetup {

	@Raptor.Dsl3
	fun kodein(configure: Kodein.Builder.() -> Unit) { // FIXME make own API
		TODO()
	}
}
