package io.fluidsonic.raptor.ktor

import io.ktor.http.*


public fun Url.toBuilder(): URLBuilder =
	URLBuilder().takeFrom(this)


public fun URLBuilder.appendParameters(parameters: Parameters): URLBuilder =
	apply { this.parameters.appendAll(parameters) }


public fun URLBuilder.appendPath(vararg components: String): URLBuilder =
	pathComponents(*components)


public fun URLBuilder.appendPath(components: List<String>): URLBuilder =
	pathComponents(components)
