package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorTransactionConfigScope {

	fun kodein(configure: Kodein.Builder.() -> Unit)
}
