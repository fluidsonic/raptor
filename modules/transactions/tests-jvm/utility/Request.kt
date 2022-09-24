package tests

import io.fluidsonic.raptor.*


data class Request(
	val id: String
) {

	companion object {

		val propertyKey = RaptorPropertyKey<Request>("request")
	}
}
