package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


/** The one and only [RaptorGraphInputScope] — the interface is stateless. */
internal object GraphInputScope : RaptorGraphInputScope {

	override fun invalid(details: String?): Nothing =
		throw GErrorException(
			GError(
				message = when (details) {
					null -> "The value is invalid."
					else -> "The value is invalid: $details"
				},
				extensions = mapOf("code" to "invalid value"),
			)
		)
}
