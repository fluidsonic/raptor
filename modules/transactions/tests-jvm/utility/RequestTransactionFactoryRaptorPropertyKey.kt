package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


object RequestTransactionFactoryRaptorPropertyKey : RaptorPropertyKey<RaptorTransactionFactory> {

	override fun toString() = "request transaction factory"
}
