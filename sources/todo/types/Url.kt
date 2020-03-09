package io.fluidsonic.raptor

import io.ktor.http.*


fun Url.toBuilder() =
	URLBuilder().takeFrom(this)


fun Url.Companion.bsonDefinition() = bsonDefinition(
	parse = ::Url,
	serialize = Url::toString
)


fun Url.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::Url)

		parseJson<String>(::Url)
		serializeJson(Url::toString)
	}
}


fun URLBuilder.appendPath(vararg components: String) =
	appendPath(components.toList())


fun URLBuilder.appendPath(components: List<String>): URLBuilder {
	encodedPath += components.joinToString(
		separator = "/",
		prefix = if (encodedPath.endsWith('/')) "" else "/"
	) { it.encodeURLQueryComponent() }

	return this
}
