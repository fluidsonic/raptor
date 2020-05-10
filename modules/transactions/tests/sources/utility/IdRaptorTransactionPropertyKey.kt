package tests.utility

import io.fluidsonic.raptor.*


object IdRaptorTransactionPropertyKey : RaptorTransactionPropertyKey<String> {

	override fun toString() = "id"
}
