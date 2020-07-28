package io.fluidsonic.raptor

import io.ktor.http.*


fun Url.toBuilder(): URLBuilder =
	URLBuilder().takeFrom(this)


fun URLBuilder.appendParameters(parameters: Parameters) =
	apply { this.parameters.appendAll(parameters) }


fun URLBuilder.appendPath(vararg components: String) =
	appendPath(components.toList())


fun URLBuilder.appendPath(components: List<String>): URLBuilder {
	encodedPath += components.joinToString(
		separator = "/",
		prefix = if (encodedPath.endsWith('/')) "" else "/"
	) { it.encodeURLQueryComponent() }

	return this
}
