package tests

import io.fluidsonic.raptor.*


object RequestRaptorPropertyKey : RaptorPropertyKey<Request> {

	override fun toString() = "request"
}
