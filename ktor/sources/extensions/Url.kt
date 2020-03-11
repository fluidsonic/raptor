package io.fluidsonic.raptor

import io.ktor.http.*


fun Url.toBuilder(): URLBuilder =
	URLBuilder().takeFrom(this)


// FIXME how to support in Bson w/o Ktor?
//fun Url.Companion.bsonDefinition() = bsonDefinition(
//	parse = ::Url,
//	serialize = Url::toString
//)


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
