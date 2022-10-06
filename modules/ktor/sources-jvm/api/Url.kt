package io.fluidsonic.raptor.ktor

import io.ktor.http.*


public fun Url.toBuilder(): URLBuilder =
	URLBuilder().takeFrom(this)


public fun URLBuilder.appendParameters(parameters: Parameters): URLBuilder =
	apply { this.parameters.appendAll(parameters) }
